package net.marvk.chess.core.board;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Board {
    default ColoredPiece getPiece(final Square square) {
        if (square == null) {
            return null;
        }

        return getPiece(square.getFile().getIndex(), square.getRank().getIndex());
    }

    default ColoredPiece getPiece(final File file, final Rank rank) {
        return getPiece(file.getIndex(), rank.getIndex());
    }

    ColoredPiece getPiece(int file, int rank);

    ColoredPiece[][] getBoard();

    default List<MoveResult> getValidMoves() {
        return getValidMovesForColor(getState().getActivePlayer());
    }

    List<MoveResult> getValidMovesForColor(Color color);

    MoveResult makeSimpleMove(final Move move);

    MoveResult makeComplexMove(final Move move, final SquareColoredPiecePair... pairs);

    BoardState getState();

    Optional<GameResult> findGameResult();

    double computeScore(final Map<Piece, Double> scoreMap, final Color color);

    default boolean isInCheck() {
        return isInCheck(getState().getActivePlayer());
    }

    boolean isInCheck(Color color);

    boolean isInCheck(Color color, Square square);
}
