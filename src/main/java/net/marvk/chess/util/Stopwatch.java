package net.marvk.chess.util;

import java.time.Duration;
import java.time.LocalDateTime;

public final class Stopwatch {
    private Stopwatch() {
        throw new AssertionError("No instances of utility class " + Stopwatch.class);
    }

    public static Duration time(final Runnable runnable) {
        final LocalDateTime start = LocalDateTime.now();
        runnable.run();
        final LocalDateTime stop = LocalDateTime.now();

        return Duration.between(start, stop);
    }
}
