package com.github.rmannibucau.log.access.core;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.rmannibucau.log.access.core.cli.CliBuilder;
import com.github.rmannibucau.log.access.core.io.LineReader;
import com.github.rmannibucau.log.access.core.service.Persistence;
import com.github.rmannibucau.log.access.core.service.StatMonitor;

public final class Launcher {
    public static void main(final String[] args) {
        final Configuration configuration = new CliBuilder(System.err)
                .option("file", "The file to monitor", false)
                .option("charset", "The charset to use to parse the file", false)
                .option("startOffset", "Where to start to poll the file from", false)
                .option("pollingPauseDuration", "Time to wait before trying to re-read the file", false)
                .option("logFormat", "Logger format for this program", false)
                .option("locale", "Locale for date parsing", false)
                .option("trafficLimit", "Ceil triggering a warning if more requests were seen during an interval", false)
                .option("trafficLimitInterval", "Interval duration for trafficLimit", false)
                .bind(Configuration.class, args)
                .orElseGet(() -> {
                    System.exit(0);
                    return null;
                });

        System.setProperty("java.util.logging.SimpleFormatter.format", configuration.logFormat);
        Locale.setDefault(ofNullable(configuration.locale).map(Locale::new).orElse(Locale.ENGLISH));

        Logger.getLogger(Launcher.class.getName())
                .log(Level.INFO, "Starting to monitor: {0}", configuration.file);

        // todo: once there is some IoC jlink friendly drop that
        final Persistence persistence = new Persistence();
        final StatMonitor monitor = new StatMonitor(
                configuration.statLogInterval,
                configuration.adjustTrafficLimitIfNeeded(),
                configuration.trafficLimitInterval);
        final LineReader reader = new LineReader(
                Paths.get(configuration.file),
                ofNullable(configuration.charset).map(Charset::forName).orElse(StandardCharsets.UTF_8),
                configuration.startOffset,
                monitor::onLine,
                offset -> persistence.onNewOffset(configuration.file, offset));
        final Runnable onClose = () -> {
            reader.cancel();
            monitor.stop();
        };
        monitor.start();

        while (true) {
            try {
                reader.iterate();
                pause(configuration.pollingPauseDuration, onClose); // we hit EOF or so so wait a bit to not use the whole CPU
            } catch (final IOException e) {
                // we should introduce an error handling, here we consider a failure will recover later
                // (when app restart?)
                pause(configuration.pollingPauseDuration, onClose);
            }
        }
    }

    private static void pause(final long duration, final Runnable onInterruption) {
        try {
            Thread.sleep(duration);
        } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
            onInterruption.run();
        }
    }

    public static class Configuration {
        private String file = "/tmp/access.log";
        private String logFormat = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp [%4$s] %5$s%6$s%n";
        private String charset = StandardCharsets.ISO_8859_1.name();
        private String locale;
        private int startOffset = 0;
        private long pollingPauseDuration = TimeUnit.SECONDS.toMillis(1);
        private long statLogInterval = TimeUnit.SECONDS.toMillis(10);
        private long trafficLimit = -1;
        private long trafficLimitInterval = TimeUnit.MINUTES.toMillis(2);

        public long adjustTrafficLimitIfNeeded() {
            return trafficLimit > 0 ? trafficLimit : (trafficLimitInterval / 1000) * 10;
        }
    }

    private Launcher() {
        // no-op
    }
}
