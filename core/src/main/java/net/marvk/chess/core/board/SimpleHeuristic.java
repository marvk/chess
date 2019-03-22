package net.marvk.chess.core.board;

import net.marvk.chess.core.bitboards.Bitboard;

public class SimpleHeuristic implements Heuristic {

    public static final int WIN = 1 << 24;
    public static final int LOSS = -WIN;
    public static final int DRAW = 0;

    //Assume no game lasts a million full moves
    private static final int MAX_FULL_MOVES = 1 << 20;

    public static boolean isCheckmateValue(final int value) {
        return value > WIN - MAX_FULL_MOVES || value < LOSS + MAX_FULL_MOVES;
    }

    /**
     * @return the heuristic value of the board from White's perspective
     */
    @Override
    public int evaluate(final Bitboard board, final boolean legalMovesRemaining) {
        if (!legalMovesRemaining) {
            if (board.isInCheck()) {
                if (board.getActivePlayer() == Color.WHITE) {
                    return LOSS + board.getFullmoveClock();
                } else {
                    return WIN - board.getFullmoveClock();
                }
            } else {
                return DRAW;
            }
        }

        if (board.getHalfmoveClock() >= 50) {
            return DRAW;
        }

        final int mySum = board.computeScore(Color.WHITE);
        final int theirSum = board.computeScore(Color.BLACK);

        final int pieceSquareValue = board.pieceSquareValue(Color.WHITE);

        return mySum - theirSum + pieceSquareValue;
    }
}
