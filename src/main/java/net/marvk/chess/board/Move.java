package net.marvk.chess.board;

public class Move {
    private final Board board;
    private final Square square;
    private final ColoredPiece coloredPiece;

    public Move(final Board board, final Square square, final ColoredPiece coloredPiece) {
        this.board = board;
        this.square = square;
        this.coloredPiece = coloredPiece;
    }
}
