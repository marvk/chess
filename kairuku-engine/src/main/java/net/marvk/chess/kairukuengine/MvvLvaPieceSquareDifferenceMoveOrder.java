package net.marvk.chess.kairukuengine;

import net.marvk.chess.core.bitboards.Bitboard;
import net.marvk.chess.core.UciMove;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MvvLvaPieceSquareDifferenceMoveOrder implements MoveOrder {
    private static final Comparator<Bitboard.BBMove> MOVE_ORDER_COMPARATOR =
            Comparator.comparing(Bitboard.BBMove::getMvvLvaSquarePieceDifferenceValue).reversed();

    @Override
    public void sort(final List<Bitboard.BBMove> moves) {
        moves.sort(MOVE_ORDER_COMPARATOR);
    }

    public void sort(final List<Bitboard.BBMove> pseudoLegalMoves, final Bitboard.BBMove previousPvMove) {
        final UciMove uciMove = previousPvMove.asUciMove();
        final Optional<Bitboard.BBMove> removed = pseudoLegalMoves.stream()
                                                                  .filter(e -> uciMove.equals(e.asUciMove()))
                                                                  .findFirst();
        sort(pseudoLegalMoves);

        if (removed.isPresent()) {
            pseudoLegalMoves.add(0, removed.get());
        }
    }
}
