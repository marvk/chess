package net.marvk.chess.kairukuengine;

import net.marvk.chess.core.util.Util;

import java.time.Duration;

public class Metrics {
    private int totalNodeCount;
    private Duration totalDuration;
    private int totalTableHits;

    private int lastNodeCount;
    private Duration lastDuration;
    private int lastTableHits;

    public Metrics() {
        resetAll();
    }

    public void incrementNodeCount() {
        lastNodeCount++;
        totalNodeCount++;
    }

    public void incrementDuration(final Duration duration) {
        lastDuration = lastDuration.plus(duration);
        totalDuration = totalDuration.plus(duration);
    }

    public void incrementTableHits() {
        lastTableHits++;
        totalTableHits++;
    }

    public int getLastNodeCount() {
        return lastNodeCount;
    }

    public Duration getLastDuration() {
        return lastDuration;
    }

    public int getLastTableHits() {
        return lastTableHits;
    }

    public int getLastNps() {
        return Util.nodesPerSecond(lastDuration, lastNodeCount);
    }

    public int getTotalNodeCount() {
        return totalNodeCount;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public int getTotalTableHits() {
        return totalTableHits;
    }

    public int getTotalNps() {
        return Util.nodesPerSecond(totalDuration, totalNodeCount);
    }

    public void resetRound() {
        lastNodeCount = 0;
        lastDuration = Duration.ZERO;
        lastTableHits = 0;
    }

    public void resetAll() {
        resetRound();
        totalNodeCount = 0;
        totalDuration = Duration.ZERO;
        totalTableHits = 0;
    }
}
