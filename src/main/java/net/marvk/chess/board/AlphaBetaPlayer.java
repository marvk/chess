package net.marvk.chess.board;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.util.Stopwatch;
import net.marvk.chess.util.Util;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Log4j2
public class AlphaBetaPlayer extends Player implements LastEvaluationGettable {
    private static final int MAX_DEPTH = 3;
    private final Heuristic heuristic;
    private int lastCount;
    private Map<Move, Double> lastEvaluation;
    private int totalCount;
    private Duration totalDuration;

    public AlphaBetaPlayer(final Color color, final Heuristic heuristic) {
        super(color);
        this.heuristic = heuristic;

        this.totalCount = 0;
        this.totalDuration = Duration.ZERO;
    }

    @Override
    public Move play(final MoveResult previousMove) {
        final AtomicReference<Pair> move = new AtomicReference<>();
        lastCount = 0;
        lastEvaluation = new HashMap<>();

        final Duration lastDuration = Stopwatch.time(() -> move.set(startExploration(previousMove)));

        final int nodesPerSecond = Util.nodesPerSecond(lastDuration, lastCount);
        final int score = move.get().score;

        log.info(getColor() + " used " + lastCount + " nodes to calculated move in " + lastDuration + " (" + nodesPerSecond + " NPS), evaluate is " + score);

        totalCount += lastCount;
        totalDuration = totalDuration.plus(lastDuration);

        final int averageNodesPerSecond = Util.nodesPerSecond(totalDuration, totalCount);

        log.info("average NPS for " + getColor() + ": " + averageNodesPerSecond);

        final double max =
                lastEvaluation.values()
                              .stream()
                              .mapToDouble(Double::doubleValue)
                              .max()
                              .orElseThrow(IllegalStateException::new);

        final List<Move> results =
                lastEvaluation.entrySet()
                              .stream()
                              .filter(kv -> Double.compare(kv.getValue(), max) == 0)
                              .map(Map.Entry::getKey)
                              .collect(Collectors.toList());

        return results.get(ThreadLocalRandom.current().nextInt(results.size()));
    }

    private Pair startExploration(final MoveResult current) {
        return alphaBeta(current, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    private Pair alphaBeta(final MoveResult current, int alpha, int beta, final int depth) {
        lastCount++;

        if (lastCount % 1000 == 0) {
            log.trace(lastCount);
        }

        if (depth == MAX_DEPTH) {
            return new Pair(current);
        }

        final List<MoveResult> validMoves = current.getBoard().getValidMoves();

        if (current.getBoard().findGameResult().isPresent()) {
            return new Pair(current);
        }

        final boolean maximise = current.getBoard().getState().getActivePlayer() == getColor();

        int value = maximise ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        validMoves.sort(Comparator.comparing(moveResult -> {
            final Board board = moveResult.getBoard();
            return board.computeScore(Util.SCORES, getColor().opposite()) - board.computeScore(Util.SCORES, getColor());
        }));

        MoveResult best = null;

        for (final MoveResult move : validMoves) {
            final Pair pair = alphaBeta(move, alpha, beta, depth + 1);

            if (depth == 0) {
                lastEvaluation.put(move.getMove(), (double) pair.score);
            }

            if (maximise) {
                if (pair.score >= value) {
                    value = pair.score;
                    best = move;
                }

                alpha = Math.max(alpha, value);
            } else {
                if (pair.score <= value) {
                    value = pair.score;
                    best = move;
                }

                beta = Math.min(beta, value);
            }

            if (beta <= alpha) {
                break;
            }
        }

        return new Pair(best, value);
    }

    @ToString
    private class Pair {
        private final MoveResult moveResult;
        private final int score;

        Pair(final MoveResult moveResult) {
            this(moveResult, heuristic.evaluate(moveResult.getBoard(), getColor()));
        }

        Pair(final MoveResult moveResult, final int score) {
            this.moveResult = moveResult;
            this.score = score;
        }
    }

    @Override
    public Map<Move, Double> getLastEvaluation() {
        return lastEvaluation;
    }
}
