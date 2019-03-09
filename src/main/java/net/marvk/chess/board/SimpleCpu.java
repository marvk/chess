package net.marvk.chess.board;

import net.marvk.chess.util.Util;

import java.util.Optional;
import java.util.Random;

public class SimpleCpu extends AlphaBetaPlayer {
    private static final Square[] SQUARES = Square.values();
    private static final Random RANDOM = new Random();
    public static final int NOISE_BOUND = 100;

    public SimpleCpu(final Color color) {
        super(color);
    }

    @Override
    protected int heuristic(final Board board) {
        final double mySum = board.computeScore(Util.SCORES, getColor());
        final double theirSum = board.computeScore(Util.SCORES, getColor().opposite());

        final Optional<GameResult> gameResult = board.findGameResult();

        if (gameResult.isPresent()) {
            final Color winner = gameResult.get().getWinner();

            if (winner == null) {
                return 0;
            }

            return winner == getColor() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        return (int) ((mySum - theirSum) * 1024) + RANDOM.nextInt(NOISE_BOUND) - NOISE_BOUND / 2;
    }
}
