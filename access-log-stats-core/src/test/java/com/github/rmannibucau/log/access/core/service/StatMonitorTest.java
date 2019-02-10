package com.github.rmannibucau.log.access.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

class StatMonitorTest {
    @Test
    void ensureStatsAreShown() {
        final Logger logger = Logger.getLogger(StatMonitor.class.getName());
        final CapturingHandler logs = new CapturingHandler();
        logger.addHandler(logs);

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "dd/LLL/yyyy:HH:mm:ss Z", Locale.getDefault());

        final CountDownLatch beforeStats = new CountDownLatch(1);
        final CountDownLatch readyToLog = new CountDownLatch(1);
        final CountDownLatch readyToReadLogs = new CountDownLatch(1);
        final StatMonitor monitor = new StatMonitor(1000, -1, -1) {
            @Override
            protected void logStats() {
                // ensure to have the 3 logs we'll send before logging otherwise our test will not be deterministic
                try {
                    beforeStats.countDown();
                    readyToLog.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                super.logStats();
                readyToReadLogs.countDown();
            }
        };
        monitor.start();

        try {
            beforeStats.await();

            // ensure it is "now" to be taken into account for the window
            final String now = formatter.format(ZonedDateTime.now()); // compute it late to avoid to overpass the delay
            monitor.onLine("127.0.0.1 - james [" + now + "] \"GET /report HTTP/1.0\" 200 123");
            monitor.onLine("127.0.0.1 - other [" + now + "] \"GET /cart/foo HTTP/1.0\" 200 123");
            monitor.onLine("127.0.0.1 - other [" + now + "] \"GET /bar/some.jsp HTTP/1.0\" 201 123");
            monitor.onLine("127.0.0.1 - jon [" + now + "] \"POST /bar/some.jsp HTTP/1.0\" 201 123");
            monitor.onLine("127.0.0.1 - rom1 [" + now + "] \"GET /bar/some.jsp?ignored_in_log HTTP/1.0\" 201 123");
            readyToLog.countDown();
            readyToReadLogs.await();

            assertEquals(1, logs.records.size());

            final LogRecord log = logs.records.iterator().next();
            assertEquals(Level.INFO, log.getLevel());
            assertEquals("Statistics at @date@:\n" +
                    "  Section: report\n" +
                    "    Total hits: 1\n" +
                    "    Hits per endpoint:\n" +
                    "      > GET /report:1\n" +
                    "  Section: cart\n" +
                    "    Total hits: 1\n" +
                    "    Hits per endpoint:\n" +
                    "      > GET /cart/foo:1\n" +
                    "  Section: bar\n" +
                    "    Total hits: 3\n" +
                    "    Hits per endpoint:\n" +
                    "      > GET /bar/some.jsp:2\n" +
                    "      > POST /bar/some.jsp:1",
                    log.getMessage().replaceFirst("Statistics at .*:\n", "Statistics at @date@:\n"));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            monitor.stop();
            logger.removeHandler(logs);
        }
    }

    @Test
    void ensureTrafficLimitIsHandled() {
        final Logger logger = Logger.getLogger(StatMonitor.class.getName());
        final CapturingHandler logs = new CapturingHandler();
        logger.addHandler(logs);

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "dd/LLL/yyyy:HH:mm:ss Z", Locale.getDefault());

        final CountDownLatch beforeLog = new CountDownLatch(1);
        final CountDownLatch readyToLog = new CountDownLatch(1);
        final CountDownLatch readyToReadLogs = new CountDownLatch(1);
        final CountDownLatch reBeforeLogs = new CountDownLatch(1);
        final CountDownLatch readyToReReadLogs = new CountDownLatch(1);
        final StatMonitor monitor = new StatMonitor(-1, 4, 1000) {
            private final AtomicInteger iteration = new AtomicInteger();

            @Override
            protected void logIfLimitReached() {
                final int it = iteration.incrementAndGet();
                if (it == 1) {
                    try {
                        beforeLog.countDown();
                        readyToLog.await();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    super.logIfLimitReached();
                    readyToReadLogs.countDown();
                } else if (it == 2) {
                    try {
                        reBeforeLogs.await();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    super.logIfLimitReached();
                    readyToReReadLogs.countDown();
                } else {
                    super.logIfLimitReached();
                }
            }
        };
        monitor.start();

        try {
            beforeLog.await();

            // let's overpass the limit
            final String now = formatter.format(ZonedDateTime.now()); // compute it late to avoid to overpass the delay
            monitor.onLine("127.0.0.1 - james [" + now + "] \"GET /report HTTP/1.0\" 200 123");
            monitor.onLine("127.0.0.1 - other [" + now + "] \"GET /cart/foo HTTP/1.0\" 200 123");
            monitor.onLine("127.0.0.1 - other [" + now + "] \"GET /bar/some.jsp HTTP/1.0\" 201 123");
            monitor.onLine("127.0.0.1 - jon [" + now + "] \"POST /bar/some.jsp HTTP/1.0\" 201 123");
            monitor.onLine("127.0.0.1 - rom1 [" + now + "] \"GET /bar/some.jsp?ignored_in_log HTTP/1.0\" 201 123");
            readyToLog.countDown();
            readyToReadLogs.await();

            assertEquals(1, logs.records.size());

            {   // ensure the message was an alert
                final LogRecord log = logs.records.iterator().next();
                logs.records.clear();
                reBeforeLogs.countDown();

                assertEquals(Level.WARNING, log.getLevel());
                assertTrue(log.getMessage().startsWith("High traffic generated an alert - hits = 5, triggered at "), log.getMessage());
            }

            {   // let it go back to something normal
                readyToReReadLogs.await();
                assertEquals(1, logs.records.size());
                final LogRecord backToNormalLog = logs.records.iterator().next();
                assertEquals(Level.INFO, backToNormalLog.getLevel(), backToNormalLog.getMessage() + " (" + backToNormalLog.getLevel() + ")");
                assertTrue(backToNormalLog.getMessage().startsWith("Traffic back to normal at "), backToNormalLog.getMessage());
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            monitor.stop();
            logger.removeHandler(logs);
        }
    }

    private static class CapturingHandler extends Handler {
        private final Collection<LogRecord> records = new CopyOnWriteArrayList<>();

        @Override
        public void publish(final LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() throws SecurityException {
            // no-op
        }
    }
}
