package net.marvk.chess.board;

import java.util.List;

public interface Board {
    ColoredPiece getPiece(Square square);

    ColoredPiece getPiece(Rank rank, File file);

    ColoredPiece getPiece(int rank, int file);

    ColoredPiece[][] getBoard();

    List<MoveResult> getValidMoves(final Color color);

    MoveResult makeMove(final Move move);
}
