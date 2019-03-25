package net.marvk.chess.kairukuengine;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Metrics {
    private int lastNegamaxNodes;
    private int lastQuiescenceNodes;
    private Duration lastDuration;
    private int lastTableHits;

    private long lastQuiescenceTerminationSum;
    private long lastQuiescenceTerminationCount;

    private int totalNegamaxNodes;
    private int totalQuiescenceNodes;
    private Duration totalDuration;
    private int totalTableHits;

    private long totalQuiescenceTerminationSum;
    private long totalQuiescenceTerminationCount;

    public Metrics() {
        resetAll();
    }

    public void incrementNegamaxNodes() {
        lastNegamaxNodes++;
        totalNegamaxNodes++;
    }

    public void incrementQuiescenceNodes() {
        lastQuiescenceNodes++;
        totalQuiescenceNodes++;
    }

    public void incrementDuration(final Duration duration) {
        lastDuration = lastDuration.plus(duration);
        totalDuration = totalDuration.plus(duration);
    }

    public void incrementTableHits() {
        lastTableHits++;
        totalTableHits++;
    }

    public void quiescenceTermination(final int depth) {
        lastQuiescenceTerminationSum += depth;
        lastQuiescenceTerminationCount++;

        totalQuiescenceTerminationSum += depth;
        totalQuiescenceTerminationCount++;
    }

    public int getLastNodes() {
        return lastNegamaxNodes + lastQuiescenceNodes;
    }

    public int getLastNegamaxNodes() {
        return lastNegamaxNodes;
    }

    public int getLastQuiescenceNodes() {
        return lastQuiescenceNodes;
    }

    public Duration getLastDuration() {
        return lastDuration;
    }

    public int getLastTableHits() {
        return lastTableHits;
    }

    public int getLastNps() {
        return nodesPerSecond(lastDuration, lastNegamaxNodes + lastQuiescenceNodes);
    }

    public double getLastTableHitRate() {
        return (double) lastTableHits / lastNegamaxNodes;
    }

    public double getLastAverageQuiescenceTerminationDepth() {
        return ((double) lastQuiescenceTerminationSum) / lastQuiescenceTerminationCount;
    }

    public int getTotalNodes() {
        return totalNegamaxNodes + totalQuiescenceNodes;
    }

    public int getTotalNegamaxNodes() {
        return totalNegamaxNodes;
    }

    public int getTotalQuiescenceNodes() {
        return totalQuiescenceNodes;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public int getTotalTableHits() {
        return totalTableHits;
    }

    public int getTotalNps() {
        return nodesPerSecond(totalDuration, totalNegamaxNodes + totalQuiescenceNodes);
    }

    public int getTotalTableHitRate() {
        return totalTableHits / totalNegamaxNodes;
    }

    public double getTotalAverageQuiescenceTerminationDepth() {
        return ((double) totalQuiescenceTerminationSum) / totalQuiescenceTerminationCount;
    }

    public void resetRound() {
        lastNegamaxNodes = 0;
        lastQuiescenceNodes = 0;
        lastDuration = Duration.ZERO;
        lastTableHits = 0;
        lastQuiescenceTerminationSum = 0L;
        lastQuiescenceTerminationCount = 0L;
    }

    public void resetAll() {
        resetRound();
        totalNegamaxNodes = 0;
        totalQuiescenceNodes = 0;
        totalDuration = Duration.ZERO;
        totalTableHits = 0;
        totalQuiescenceTerminationSum = 0L;
        totalQuiescenceTerminationCount = 0L;
    }

    private static int nodesPerSecond(final Duration duration, final int nodes) {
        return (int) Math.round(((double) nodes / duration.toNanos()) * TimeUnit.SECONDS.toNanos(1));
    }
}
