package net.marvk.chess.board;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.util.Stopwatch;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public abstract class AlphaBetaPlayer extends Player {
    private static final int MAX_DEPTH = 3;
    private int count;

    public AlphaBetaPlayer(final Color color) {
        super(color);
    }

    @Override
    public Move play(final MoveResult previousMove) {
        final AtomicReference<Pair> move = new AtomicReference<>();
        count = 0;

        final Duration duration = Stopwatch.time(() -> move.set(startExploration(previousMove)));

        final int nodesPerSecond = (int) Math.round(((double) count / duration.toNanos()) * TimeUnit.SECONDS.toNanos(1));

        log.info("Player used " + count + " nodes to calculated move in " + duration + " (" + nodesPerSecond + " NPS), heuristic is " + move
                .get().score);

        return move.get().moveResult.getMove();
    }

    private Pair startExploration(final MoveResult current) {
        return alphaBeta(current, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    }

    private Pair alphaBeta(final MoveResult current, int alpha, int beta, final int depth) {
        count++;

        if (count % 1000 == 0) {
            log.trace(count);
        }

        if (depth == MAX_DEPTH) {
            return new Pair(current);
        }

        final List<MoveResult> validMoves = current.getBoard().getValidMoves();

        if (validMoves.isEmpty()) {
            return new Pair(current);
        }

        final boolean maximise = current.getBoard().getState().getActivePlayer() == getColor();

        int value = maximise ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        MoveResult best = null;

        for (final MoveResult move : validMoves) {
            final Pair pair = alphaBeta(move, alpha, beta, depth + 1);

            if (maximise) {
                if (pair.score > value) {
                    value = pair.score;
                    best = move;
                }

                alpha = Math.max(alpha, value);
            } else {
                if (pair.score < value) {
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

    protected abstract int heuristic(final Board board);
}
