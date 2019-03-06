package net.marvk.chess.board;

import java.util.List;

public interface Board {
    ColoredPiece getPiece(Square square);

    ColoredPiece getPiece(Rank rank, File file);

    ColoredPiece getPiece(int rank, int file);

    ColoredPiece[][] getBoard();

    List<Move> getValidMoves(final Color color);

    Move makeMove(final ColoredPiece coloredPiece, final Square square);
}
