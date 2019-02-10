package com.github.rmannibucau.log.access.core.io;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

class LineReaderTest {
    @Test
    void simpleFileRead(@TempDir final Path temp, final TestInfo testInfo) throws IOException {
        final Path source = temp.resolve(getTestId(testInfo));
        Files.writeString(source, "first line\nsecond line\nlast line\n",
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        final List<String> lines = new ArrayList<>(3);
        final List<Long> positions = new ArrayList<>(1);
        new LineReader(source, StandardCharsets.UTF_8, 0L, lines::add, positions::add).iterate();
        assertEquals(asList("first line", "second line", "last line"), lines);
        assertEquals(asList(11L, 23L, 33L), positions);
    }

    @Test
    void continuousReading(@TempDir final Path temp, final TestInfo testInfo) throws IOException {
        final String testId = getTestId(testInfo);
        final Path source = temp.resolve(testId);
        Files.writeString(source, "first line\nsecond line\nlast line\n",
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        final List<String> lines = new ArrayList<>(16);
        final List<Long> positions = new ArrayList<>();
        final AtomicBoolean finished = new AtomicBoolean();
        final RuntimeException errors = new RuntimeException(); // for errors happening in another thread than junit one

        new Thread(() -> {
            final LineReader poller = new LineReader(source, StandardCharsets.UTF_8, 0L, line -> {
                synchronized (lines) {
                    lines.add(line);
                }
            }, offset -> {
                synchronized (positions) {
                    positions.add(offset);
                }
            });
            while (!finished.get()) {
                try {
                    poller.iterate();
                } catch (final IOException e) {
                    errors.addSuppressed(e);
                }
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, testId + "##reader").start();

        final BlockingQueue<Runnable> writerTasks = new ArrayBlockingQueue<>(10);
        final Runnable poisonPill = () -> {};
        new Thread(() -> {
            Runnable task;
            try {
                do {
                    task = writerTasks.take();
                    if (task == poisonPill) {
                        break;
                    }
                    task.run();
                } while (true);
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }, testId + "##writer").start();

        // ensure file is read
        until("file not read", () -> {
            synchronized (lines) {
                return lines.size() == 3;
            }
        }, 1, MINUTES);
        until("position not set", () -> {
            synchronized (lines) {
                return positions.size() == 3;
            }
        }, 1, MINUTES);
        assertEquals(asList(11L, 23L, 33L), positions);

        final Consumer<String> addLineAndAssert = line -> {
            final CountDownLatch latch = new CountDownLatch(1);
            final int expectedNewSize = lines.size() + 1;
            writerTasks.add(() -> {
                try {
                    Files.writeString(source, line + '\n', StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                } catch (final IOException e) {
                    errors.addSuppressed(e);
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            until("new line not read", () -> expectedNewSize == lines.size(), 10, SECONDS);
            assertEquals(line, lines.get(expectedNewSize - 1));
        };
        for (int i = 1; i <= 10; i++) {
            addLineAndAssert.accept("new line " + i);
        }

        until("positions should be set", () -> positions.size() == 13, 30, SECONDS);
        assertEquals(144, positions.get(positions.size() - 1));

        finished.set(true);
        writerTasks.add(poisonPill); // end
        if (errors.getSuppressed().length > 0) {
            fail(errors);
        }
    }

    @Test
    void restart(@TempDir final Path temp, final TestInfo testInfo) throws IOException {
        final String testId = getTestId(testInfo);
        final Path source = temp.resolve(testId);
        Files.writeString(source, "first line\nsecond line\nlast line\nanother one\nand new last one\n",
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        final List<String> lines = new ArrayList<>(3);
        final List<Long> positions = new ArrayList<>(1);
        new LineReader(source, StandardCharsets.UTF_8, 33L, lines::add, positions::add).iterate();
        assertEquals(asList("another one", "and new last one"), lines);
        assertEquals(asList(45L, 62L), positions);
    }

    private void until(final String message, final Supplier<Boolean> test, final int timeout, final TimeUnit unit) {
        final Clock clock = Clock.systemUTC();
        final Instant end = clock.instant().plusMillis(unit.toMillis(timeout));
        while (clock.instant().isBefore(end)) {
            if (test.get()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        fail(message);
    }

    private String getTestId(final TestInfo testInfo) {
        return testInfo.getTestClass().orElseThrow().getName() + "_" + testInfo.getTestMethod().orElseThrow().getName();
    }
}
