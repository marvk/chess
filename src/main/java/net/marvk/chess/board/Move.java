package net.marvk.chess.board;

public class Move {
    private final Square source;
    private final Square target;
    private final ColoredPiece coloredPiece;
    private final ColoredPiece promoteTo;

    public Move(final Square source, final Square target, final ColoredPiece coloredPiece, final ColoredPiece promoteTo) {
        this.source = source;
        this.target = target;
        this.coloredPiece = coloredPiece;
        this.promoteTo = promoteTo;
    }

    public Move(final Square source, final Square target, final ColoredPiece coloredPiece) {
        this(source, target, coloredPiece, null);
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

    public ColoredPiece getPromoteTo() {
        return promoteTo;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Move move = (Move) o;

        if (source != move.source) return false;
        if (target != move.target) return false;
        if (coloredPiece != move.coloredPiece) return false;
        return promoteTo == move.promoteTo;

    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (coloredPiece != null ? coloredPiece.hashCode() : 0);
        result = 31 * result + (promoteTo != null ? promoteTo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "source=" + source +
                ", target=" + target +
                ", coloredPiece=" + coloredPiece +
                ", promoteTo=" + promoteTo +
                '}';
    }
}
