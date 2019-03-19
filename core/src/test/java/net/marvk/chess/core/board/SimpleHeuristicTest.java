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

        final int expected = 100 + 100 + 100 + 100 + 100 - 100 - 100 - 100 - 100 + 330 + 500 + 500 - 5 - 10 - 10 - 20 + 10 + 10 + 5 - 10 - 30 + 30;

        Assertions.assertEquals(expected, value);
    }
}