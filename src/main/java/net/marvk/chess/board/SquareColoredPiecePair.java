package net.marvk.chess.board;

public class SquareColoredPiecePair {
    private final Square square;
    private final ColoredPiece coloredPiece;

    public SquareColoredPiecePair(final Square square, final ColoredPiece coloredPiece) {
        this.square = square;
        this.coloredPiece = coloredPiece;
    }

    public Square getSquare() {
        return square;
    }

    public ColoredPiece getColoredPiece() {
        return coloredPiece;
    }
}
