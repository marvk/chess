package net.marvk.chess.core.board;

public interface Heuristic {
    int evaluate(final Board board, final Color self);
}
