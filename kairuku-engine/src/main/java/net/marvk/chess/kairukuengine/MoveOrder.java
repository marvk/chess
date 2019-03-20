package net.marvk.chess.kairukuengine;

import net.marvk.chess.core.bitboards.Bitboard;

import java.util.List;

@FunctionalInterface
public interface MoveOrder {
    void sort(final List<Bitboard.BBMove> moves);
}
