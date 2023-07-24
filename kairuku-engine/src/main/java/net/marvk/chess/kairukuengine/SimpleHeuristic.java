package net.marvk.chess.kairukuengine;

import net.marvk.chess.core.Fen;
import net.marvk.chess.core.bitboards.Bitboard;
import net.marvk.chess.core.Color;

public class SimpleHeuristic implements Heuristic {

    public static final int WIN = 1 << 24;
    public static final int LOSS = -WIN;
    public static final int DRAW = 0;

    //Assume no game lasts a million full moves
    private static final int MAX_FULL_MOVES = 1 << 20;
    private static final int MAX_HALF_MOVES = 50;

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

        if (board.getHalfmoveClock() >= MAX_HALF_MOVES) {
            return DRAW;
        }

        final int mySum = board.computeScore(Color.WHITE);
        final int theirSum = board.computeScore(Color.BLACK);

        final int pieceSquareValue = board.pieceSquareValue(Color.WHITE);

//        System.out.println(mySum);
//        System.out.println(theirSum);
//        System.out.println(pieceSquareValue);

        return mySum - theirSum + pieceSquareValue;
    }

    public static void main(String[] args) {
        System.out.println(new SimpleHeuristic().evaluate(new Bitboard(Fen.parse("rn2k2r/ppp2ppp/8/3pPP2/3P1q2/P1KB4/P1P4P/3R2N1 b kq - 0 14")), true));
        System.out.println(new SimpleHeuristic().evaluate(new Bitboard(Fen.parse("rn2k2r/ppp2ppp/8/3pPP2/3P1q2/P1KB4/P1P4P/3R2N1 w kq - 0 14")), true));
    }
}
