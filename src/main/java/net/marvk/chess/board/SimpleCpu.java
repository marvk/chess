package net.marvk.chess.board;

import java.util.*;

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
        final double score = Arrays.stream(SQUARES)
                                 .map(board::getPiece)
                                 .filter(Objects::nonNull)
                                 .mapToDouble(this::score)
                                 .sum();

        return (int) (score * 10.0);
    }

    private double score(final ColoredPiece piece) {
        final double score = SCORES.get(piece.getPiece());

        return piece.getColor() == getColor() ? score : -score;
    }
}
