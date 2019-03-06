package net.marvk.chess.board;

public class BoardState {
    private final Color activePlayer;

    private final boolean whiteCastleKing;
    private final boolean whiteCastleQueen;
    private final boolean blackCastleKing;
    private final boolean blackCastleQueen;

    private final Square enPassantTargetSquare;

    private final int halfmoveClock;
    private final int fullmoveClock;

    public BoardState(final Fen fen) {
        this.activePlayer = Color.getColorFromFen(fen.getActiveColor());

        final String castlingAvailability = fen.getCastlingAvailability();

        this.whiteCastleKing = castlingAvailability.contains(Character.toString(ColoredPiece.WHITE_KING.getSan()));
        this.whiteCastleQueen = fen.getCastlingAvailability().contains(Character.toString(ColoredPiece.WHITE_QUEEN.getSan()));
        this.blackCastleKing = fen.getCastlingAvailability().contains(Character.toString(ColoredPiece.BLACK_KING.getSan()));
        this.blackCastleQueen = fen.getCastlingAvailability().contains(Character.toString(ColoredPiece.BLACK_QUEEN.getSan()));

        this.enPassantTargetSquare = Square.getSquareFromFen(fen.getEnPassantTargetSquare());

        this.halfmoveClock = Integer.parseInt(fen.getHalfmoveClock());
        this.fullmoveClock = Integer.parseInt(fen.getFullmoveClock());
    }

    public Color getActivePlayer() {
        return activePlayer;
    }

    public boolean canWhiteCastleKing() {
        return whiteCastleKing;
    }

    public boolean canWhiteCastleQueen() {
        return whiteCastleQueen;
    }

    public boolean canBlackCastleKing() {
        return blackCastleKing;
    }

    public boolean canBlackCastleQueen() {
        return blackCastleQueen;
    }

    public Square getEnPassantTargetSquare() {
        return enPassantTargetSquare;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public int getFullmoveClock() {
        return fullmoveClock;
    }
}
