package net.marvk.chess.board;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleCpu extends AlphaBetaPlayer {
    private static final Map<Piece, Double> SCORES;
    private static final Square[] SQUARES = Square.values();

    static {
        final Map<Piece, Double> map = new EnumMap<>(Piece.class);

        map.put(Piece.KING, 0.);
        map.put(Piece.QUEEN, 9.);
        map.put(Piece.ROOK, 5.);
        map.put(Piece.BISHOP, 3.5);
        map.put(Piece.KNIGHT, 3.);
        map.put(Piece.PAWN, 1.);

        SCORES = Collections.unmodifiableMap(map);
    }

    public SimpleCpu(final Color color) {
        super(color);
    }

    @Override
    protected int heuristic(final Board board) {
        double mySum = 0;
        double theirSum = 0;

        final Optional<GameResult> gameResult = board.findGameResult();

        if (gameResult.isPresent()) {
            final Color winner = gameResult.get().getWinner();

            if (winner == null) {
                return 0;
            }

            return winner == getColor() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        for (final Square square : SQUARES) {
            final ColoredPiece piece = board.getPiece(square);

            if (piece != null) {
                final double score = score(piece);

                if (piece.getColor() == getColor()) {
                    mySum += score;
                } else {
                    theirSum += score;
                }
            }
        }

        return (int) ((mySum - theirSum) * 1024) + ThreadLocalRandom.current().nextInt(100);
    }

    private double score(final ColoredPiece piece) {
        return SCORES.get(piece.getPiece());
    }
}
