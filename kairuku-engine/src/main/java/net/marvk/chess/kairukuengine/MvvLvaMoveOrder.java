package net.marvk.chess.kairukuengine;

import net.marvk.chess.core.bitboards.Bitboard;

import java.util.Comparator;
import java.util.List;

public class MvvLvaMoveOrder implements MoveOrder{
    private static final Comparator<Bitboard.BBMove> MOVE_ORDER_COMPARATOR =
            Comparator.comparing(Bitboard.BBMove::getMvvLvaValue).reversed();

    @Override
    public void sort(final List<Bitboard.BBMove> moves) {
        moves.sort(MOVE_ORDER_COMPARATOR);
    }
}
