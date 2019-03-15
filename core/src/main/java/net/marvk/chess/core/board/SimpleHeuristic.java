package net.marvk.chess.core.board;

import net.marvk.chess.core.util.Util;

import java.util.Optional;
import java.util.Random;

public class SimpleHeuristic implements Heuristic {
    private static final Random RANDOM = new Random();
    private static final int NOISE_BOUND = 0;

    @Override
    public int evaluate(final Board board, final Color self) {
        final double mySum = board.computeScore(Util.SCORES, self);
        final double theirSum = board.computeScore(Util.SCORES, self.opposite());

        final Optional<GameResult> gameResult = board.findGameResult();

        if (gameResult.isPresent()) {
            final Color winner = gameResult.get().getWinner();

            if (winner == null) {
                return 0;
            }

            final int timePenalty = board.getState().getFullmoveClock();

            return winner == self ? Integer.MAX_VALUE - timePenalty : Integer.MIN_VALUE;
        }

        final int noise;

        if (NOISE_BOUND > 0) {
            noise = RANDOM.nextInt(NOISE_BOUND) - NOISE_BOUND / 2;
        } else {
            noise = 0;
        }

        return (int) ((mySum - theirSum) * 1024) + noise;
    }
}
