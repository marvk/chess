package net.marvk.chess.board;

public class Move {
    public static final Move NULL_MOVE = new Move(null, null, null);

    private final Square source;
    private final Square target;
    private final ColoredPiece coloredPiece;
    private final ColoredPiece promoteTo;
    private final boolean castling;
    private final boolean enPassant;
    private final boolean pawnDoubleMove;

    private Move(final Square source,
                 final Square target,
                 final ColoredPiece coloredPiece,
                 final ColoredPiece promoteTo,
                 final boolean castling,
                 final boolean enPassant,
                 final boolean pawnDoubleMove) {
        this.source = source;
        this.target = target;
        this.coloredPiece = coloredPiece;
        this.promoteTo = promoteTo;
        this.castling = castling;
        this.enPassant = enPassant;
        this.pawnDoubleMove = pawnDoubleMove;
    }

    private Move(final Square source, final Square target, final ColoredPiece coloredPiece) {
        this(source, target, coloredPiece, null, false, false, false);
    }

    public static Move simple(final Square source, final Square target, final ColoredPiece coloredPiece) {
        return new Move(source, target, coloredPiece);
    }

    public static Move castling(final Square source, final Square target, final ColoredPiece coloredPiece) {
        return new Move(source, target, coloredPiece, null, true, false, false);
    }

    public static Move enPassant(final Square source, final Square target, final ColoredPiece coloredPiece) {
        return new Move(source, target, coloredPiece, null, false, true, false);
    }

    public static Move pawnDoubleMove(final Square source, final Square target, final ColoredPiece coloredPiece) {
        return new Move(source, target, coloredPiece, null, false, true, true);
    }

    public static Move promotion(final Square source, final Square target, final ColoredPiece coloredPiece, final ColoredPiece promoteTo) {
        return new Move(source, target, coloredPiece, promoteTo, false, true, false);
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

    public boolean isPromotion() {
        return promoteTo != null;
    }

    public boolean isCastling() {
        return castling;
    }

    public boolean isEnPassant() {
        return enPassant;
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

    public boolean isPawnDoubleMove() {
        return pawnDoubleMove;
    }

    public String getUci() {
        final String squares = source.getFen() + target.getFen();

        if (isPromotion()) {
            return squares + Character.toLowerCase(promoteTo.getSan());
        } else {
            return squares;
        }
    }
}
