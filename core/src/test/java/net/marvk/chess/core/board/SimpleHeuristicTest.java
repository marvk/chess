package net.marvk.chess.core.board;

import net.marvk.chess.core.bitboards.Bitboard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleHeuristicTest {

    @Test
    void evaluate() {
        final Fen fen = Fen.parse("r1b5/1ppp2k1/p7/1r4p1/6P1/8/PP3P2/6K1 b - - 1 35");

        final Bitboard board = new Bitboard(fen);
        board.getValidMoves();

        final int value = new SimpleHeuristic().evaluate(board, Color.BLACK, true);

        final int blackPawns = 100 + 100 + 100 + 100 + 100;
        final int whitePawns = 100 + 100 + 100 + 100;
        final int blackBishop = 330;
        final int blackRooks = 500 + 500;

        final int blackPawnPosition = 0 + (-20) + 10 + 10 + 5;
        final int blackBishopPosition = -10;
        final int blackRookPosition = 0;
        final int whitePawnPosition = 5 + 10 + 10 + 0;

        final int blackPosition = blackPawnPosition + blackBishopPosition + blackRookPosition;
        final int whitePosition = whitePawnPosition;

        final int blackPieces = blackPawns + blackBishop + blackRooks;
        final int whitePieces = whitePawns;

        System.out.println();
        System.out.println("blackPieces = " + blackPieces);
        System.out.println("whitePieces = " + whitePieces);

        System.out.println("blackPosition = " + blackPosition);
        System.out.println("whitePosition = " + whitePosition);
        System.out.println();

        final int expected = blackPawns + blackBishop + blackRooks + blackPosition - whitePawns - whitePawnPosition;

        Assertions.assertEquals(expected, value);
    }
}