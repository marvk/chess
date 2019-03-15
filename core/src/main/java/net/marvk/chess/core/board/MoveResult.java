package net.marvk.chess.core.board;

public class MoveResult {
    private final Board board;
    private final Move move;

    public MoveResult(final Board board, final Move move) {
        this.board = board;
        this.move = move;
    }

    public Board getBoard() {
        return board;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MoveResult that = (MoveResult) o;

        if (board != null ? !board.equals(that.board) : that.board != null) return false;
        return move != null ? move.equals(that.move) : that.move == null;

    }

    @Override
    public int hashCode() {
        int result = board != null ? board.hashCode() : 0;
        result = 31 * result + (move != null ? move.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MoveResult{" +
                "board=" + board +
                ", move=" + move +
                '}';
    }
}
