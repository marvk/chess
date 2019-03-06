package net.marvk.chess.board;

public class MoveResult {
    private final Board board;
    private final Move move;

    public MoveResult(final Board board, final Move move) {
        this.board = board;
        this.move = move;
    }
}
