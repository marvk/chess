package net.marvk.chess.core.board;

import net.marvk.chess.core.bitboards.Bitboard;

public class SimpleHeuristic implements Heuristic {

    @Override
    public int evaluate(final Bitboard board, final Color self, final boolean legalMovesRemaining) {
        if (!legalMovesRemaining) {
            if (board.isInCheck()) {
                if (board.getActivePlayer() == self) {
                    return Integer.MIN_VALUE;
                } else {
                    return Integer.MAX_VALUE - board.getFullmoveClock();
                }
            } else {
                return 0;
            }
        }

        final int mySum = board.computeScore(self);
        final int theirSum = board.computeScore(self.opposite());

        final int pieceSquareValue = board.pieceSquareValue(self);

        return mySum - theirSum + pieceSquareValue;
    }
}
