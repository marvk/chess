package net.marvk.chess.board;

import java.util.List;

public interface Board {
    ColoredPiece getPiece(Square square);

    ColoredPiece getPiece(File file, Rank rank);

    ColoredPiece getPiece(int file, int rank);

    ColoredPiece[][] getBoard();

    List<MoveResult> getValidMoves(final Color color);

    MoveResult makeSimpleMove(final Move move);

    MoveResult makeComplexMove(final Move move, final SquareColoredPiecePair... pairs);

    BoardState getState();

    boolean isInCheck(Color color);

    boolean isInCheck(Color color, Square square);
}
