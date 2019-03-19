package net.marvk.chess.core.board;

import net.marvk.chess.core.bitboards.Bitboard;

@FunctionalInterface
public interface Heuristic {
    int evaluate(Bitboard board, boolean legalMovesRemaining);
}
