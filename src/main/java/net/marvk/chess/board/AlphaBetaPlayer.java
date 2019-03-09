package net.marvk.chess.board;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.util.Stopwatch;
import net.marvk.chess.util.Util;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public abstract class AlphaBetaPlayer extends Player implements LastEvaluationGettable {
    private static final int MAX_DEPTH = 5;
    private int lastCount;
    private Map<Move, Double> lastEvaluation;

    public AlphaBetaPlayer(final Color color) {
        super(color);
    }

    @Override
    public Move play(final MoveResult previousMove) {
        final AtomicReference<Pair> move = new AtomicReference<>();
        lastCount = 0;
        lastEvaluation = new HashMap<>();

        final Duration duration = Stopwatch.time(() -> move.set(startExploration(previousMove)));

        final int nodesPerSecond = (int) Math.round(((double) lastCount / duration.toNanos()) * TimeUnit.SECONDS.toNanos(1));

        log.info(getColor() + " used " + lastCount + " nodes to calculated move in " + duration + " (" + nodesPerSecond + " NPS), heuristic is " + move
                .get().score);

        return move.get().moveResult.getMove();
    }

    private Pair startExploration(final MoveResult current) {
        return alphaBeta(current, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    private Pair alphaBeta(final MoveResult current, int alpha, int beta, final int depth) {
        lastCount++;

        if (lastCount % 1000 == 0) {
            log.trace(lastCount);
        }

        if (depth == MAX_DEPTH - 1) {
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
            this(moveResult, heuristic(moveResult.getBoard()));
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

    protected abstract int heuristic(final Board board);
}
