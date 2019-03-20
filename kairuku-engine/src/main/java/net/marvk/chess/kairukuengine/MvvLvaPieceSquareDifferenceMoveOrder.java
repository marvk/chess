package net.marvk.chess.kairukuengine;

import net.marvk.chess.core.bitboards.Bitboard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MvvLvaPieceSquareDifferenceMoveOrder implements MoveOrder {
    private static final Comparator<Bitboard.BBMove> MOVE_ORDER_COMPARATOR =
            Comparator.comparing(Bitboard.BBMove::getMvvLvaSquarePieceDifferenceValue).reversed();

    @Override
    public void sort(final List<Bitboard.BBMove> moves) {
        Collections.shuffle(moves);
        moves.sort(MOVE_ORDER_COMPARATOR);
    }
}
