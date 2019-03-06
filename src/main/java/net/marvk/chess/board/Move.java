package net.marvk.chess.board;

public class Move {
    private final Square source;
    private final Square target;
    private final ColoredPiece coloredPiece;

    public Move(final Square source, final Square target, final ColoredPiece coloredPiece) {
        this.source = source;
        this.target = target;
        this.coloredPiece = coloredPiece;
    }

    public Square getSource() {
        return source;
    }

    public Square getTarget() {
        return target;
    }

    public ColoredPiece getColoredPiece() {
        return coloredPiece;
    }
}
