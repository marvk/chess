package net.marvk.chess.board;

public final class Boards {
    private Boards() {
        throw new AssertionError("No instances of utility class " + Boards.class);
    }

    private static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static Board startingPosition() {
        return new Board(STARTING_POSITION);
    }
}
