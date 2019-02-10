package com.github.rmannibucau.log.access.core.service;

import static java.time.Clock.systemDefaultZone;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.rmannibucau.log.access.core.parser.api.Line;
import com.github.rmannibucau.log.access.core.parser.api.Parser;
import com.github.rmannibucau.log.access.core.parser.impl.RequestParser;
import com.github.rmannibucau.log.access.core.parser.impl.W3CCommonLogHttpParser;

// todo: split it in 2 or 3 services
public class StatMonitor {
    private static final Logger LOGGER = Logger.getLogger(StatMonitor.class.getName());

    private final Parser logLineParser = new W3CCommonLogHttpParser();
    private final Parser requestParser = new RequestParser();
    private final DateParser dateParser = new DateParser();
    private final LongAdder globalHits = new LongAdder();

    private final long statLogDelay;
    private final long trafficLimit;
    private final long trafficInterval;

    private volatile boolean trafficLimitReached = false;

    // here we assume we can store all data between 2 log iterations
    // if not we must move to another storage and probably output as well
    private final Map<String, Collection<Point>> hitsPerSection = new ConcurrentHashMap<>();

    private final Clock clock = systemDefaultZone();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> statTask;
    private ScheduledFuture<?> limitTask;

    public StatMonitor(final long statLogDelay, final long trafficLimit, final long trafficInterval) {
        this.statLogDelay = statLogDelay;
        this.trafficLimit = trafficLimit;
        this.trafficInterval = trafficInterval;
    }

    public void onLine(final String line) {
        final Line parsed = logLineParser.parse(line.toCharArray());
        if (!parsed.isValid()) {
            LOGGER.log(SEVERE, "Invalid line {0}", line);
            return;
        }
        LOGGER.log(FINE, "Valid line {0}", line);
        final String request = parsed.getData().get("request");
        final Line req = requestParser.parse(request.toCharArray());
        if (!req.isValid()) {
            LOGGER.log(SEVERE, "Invalid request in line {0}", line);
            return;
        }
        LOGGER.log(FINE, "Valid request {0}", line);

        globalHits.add(1);

        final String section = getSection(req.getData().get("path"));

        // computeIfAbsent does not make sense here cause we'll get it most of the time
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            Collection<Point> points = hitsPerSection.get(section);
            if (points == null) {
                points = new ArrayList<>(16);
                final Collection<Point> existing = hitsPerSection.putIfAbsent(section, points);
                if (existing != null) {
                    points = existing;
                }
            }

            // note: we could use the date in the request
            final Point point = new Point(
                    dateParser.parse(parsed.getData().get("date"), clock),
                    req.getData().get("method"),
                    req.getData().get("path"));
            synchronized (points) {
                points.add(point);
            }
        } finally {
            lock.unlock();
        }
    }

    protected void logIfLimitReached() {
        final long total = globalHits.sumThenReset();
        if (total > trafficLimit) {
            trafficLimitReached = true;
            LOGGER.warning(() -> "High traffic generated an alert - hits = " + total + ", triggered at " + LocalDateTime.now());
        } else if (trafficLimitReached) {
            trafficLimitReached = false;
            LOGGER.info(() -> "Traffic back to normal at " + LocalDateTime.now());
        } else {
            LOGGER.fine(() -> "Current traffic limit: " + total);
        }
    }

    private String getSection(final String path) {
        final String normalized = path.length() >= 1 && path.charAt(0) == '/' ? path.substring(1) : path;
        final int sep = normalized.indexOf('/');
        return sep < 0 ? normalized : normalized.substring(0, sep);
    }

    protected void logStats() {
        final long intervalStart = clock.instant()
                .truncatedTo(ChronoUnit.MILLIS)
                .minusMillis(statLogDelay)
                .toEpochMilli();

        // remove outdated points
        hitsPerSection.values().forEach(points -> {
            synchronized (points) {
                points.removeIf(pt -> pt.timestamp < intervalStart);
            }
        });
        final List<String> bucketsToRemove = hitsPerSection.entrySet().stream().filter(it -> {
            synchronized (it) {
                return it.getValue().isEmpty();
            }
        }).map(Map.Entry::getKey).collect(toList());
        if (!bucketsToRemove.isEmpty()) {
            final Lock lock = this.lock.writeLock();
            lock.lock();
            try {
                bucketsToRemove.stream()
                        .filter(key -> {
                            final Collection<Point> points = hitsPerSection.get(key);
                            return points != null && points.isEmpty();
                        })
                        .forEach(hitsPerSection::remove);
            } finally {
                lock.unlock();
            }
        }

        // copy data to work off lock
        final Map<String, Collection<Point>> data;
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            // copy to compute stats off lock
            data = hitsPerSection.entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
        } finally {
            lock.unlock();
        }

        // format and log
        final String prefix = "Statistics at " + new Date();
        if (data.isEmpty()) {
            LOGGER.info(() -> prefix + ": no data");
        } else {
            LOGGER.info(() -> prefix + ":\n" +
                    data.keySet().stream()
                            .sorted(comparing(it -> data.get(it).size()))
                            .map(section -> toStat(section, data.get(section)))
                            .collect(joining("\n")));
        }
    }

    private String toStat(final String section, final Collection<Point> points) {
        return "  Section: " + section + "\n" +
                "    Total hits: " + points.size() + "\n" +
                "    Hits per endpoint:" +
                points.stream()
                    .map(p -> p.method + " " + p.path)
                    .collect(groupingBy(identity(), counting()))
                    .entrySet().stream()
                    .map(entry -> "      > " + entry.getKey() + ":" + entry.getValue())
                    .sorted()
                    .collect(Collectors.joining("\n", "\n", ""));
    }

    public void start() {
        executor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(r, getClass().getName() + '-' + counter.incrementAndGet());
            }
        });
        if (statLogDelay > 0) {
            statTask = executor.scheduleAtFixedRate(this::logStats, statLogDelay, statLogDelay, MILLISECONDS);
        }
        if (trafficLimit > 0) {
            limitTask = executor.scheduleAtFixedRate(this::logIfLimitReached, trafficInterval, trafficInterval, MILLISECONDS);
        }
    }

    public void stop() {
        Stream.of(statTask, limitTask)
                .forEach(future -> ofNullable(future).ifPresent(f -> f.cancel(true)));
        ofNullable(executor).ifPresent(e -> {
            e.shutdownNow();
            try {
                e.awaitTermination(10, SECONDS);
            } catch (final InterruptedException e1) {
                Thread.currentThread().interrupt(); // not a big deal here
            }
        });
    }

    // todo: add user too
    private static class Point {
        private final long timestamp;
        private final String method;
        private final String path;

        private Point(final Instant timestamp, final String method, final String path) {
            this.timestamp = timestamp.truncatedTo(ChronoUnit.MILLIS).toEpochMilli();
            this.method = method;
            this.path = stripQuery(path);
        }

        private String stripQuery(final String path) {
            final int q = path.indexOf('?');
            return q > 0 ? path.substring(0, q) : path;
        }
    }
}
