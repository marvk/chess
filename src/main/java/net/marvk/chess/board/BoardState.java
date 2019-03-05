package net.marvk.chess.board;

public class BoardState {
    private final Color turn;

    private final boolean whiteCastleKing;
    private final boolean whiteCastleQueen;
    private final boolean blackCastleKing;
    private final boolean blackCastleQueen;

    private final Square enPassant;

    private final int halfmoveClock;
    private final int fullmoveClock;

    public BoardState(final String[] split) {
        final String turn = split[1];
        final String castle = split[2];
        final String enPassant = split[3];
        final String halfMoveClock = split[4];
        final String fullMoveClock = split[5];

        this.turn = Color.getColorFromFen(turn);

        this.whiteCastleKing = castle.contains(Character.toString(ColoredPiece.WHITE_KING.getSan()));
        this.whiteCastleQueen = castle.contains(Character.toString(ColoredPiece.WHITE_QUEEN.getSan()));
        this.blackCastleKing = castle.contains(Character.toString(ColoredPiece.BLACK_KING.getSan()));
        this.blackCastleQueen = castle.contains(Character.toString(ColoredPiece.BLACK_QUEEN.getSan()));

        this.enPassant = Square.getSquareFromFen(enPassant);

        this.halfmoveClock = Integer.parseInt(halfMoveClock);
        this.fullmoveClock = Integer.parseInt(fullMoveClock);
    }
}
