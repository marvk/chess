package net.marvk.chess.board;

public interface Heuristic {
    int evaluate(final Board board, final Color self);
}
