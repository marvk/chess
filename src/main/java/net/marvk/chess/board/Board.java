package net.marvk.chess.board;

import java.util.List;

public interface Board {
    ColoredPiece getPiece(Square square);

    ColoredPiece getPiece(File file, Rank rank);

    ColoredPiece getPiece(int file, int rank);

    ColoredPiece[][] getBoard();

    List<MoveResult> getValidMoves(final Color color);

    MoveResult makeMove(final Move move);
}
