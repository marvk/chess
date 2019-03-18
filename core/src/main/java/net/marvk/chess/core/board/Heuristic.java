package net.marvk.chess.core.board;

import net.marvk.chess.core.bitboards.Bitboard;

public interface Heuristic {
    int evaluate(final Bitboard board, final Color self);
}
