package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Square;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static net.marvk.chess.core.bitboards.MoveConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveConstantsTest {
    private static final long[] MASKS = {
            PIECE_MOVED_MASK,
            PIECE_ATTACKED_MASK,
            SELF_LOST_KING_SIDE_CASTLE_MASK,
            SELF_LOST_QUEEN_SIDE_CASTLE_MASK,
            OPPONENT_LOST_KING_SIDE_CASTLE_MASK,
            OPPONENT_LOST_QUEEN_SIDE_CASTLE_MASK,
            CASTLE_MOVE_MASK,
            EN_PASSANT_ATTACK_MASK,
            SOURCE_SQUARE_INDEX_MASK,
            TARGET_SQUARE_INDEX_MASK,
            HALFMOVE_RESET_MASK,
            PREVIOUS_HALFMOVE_MASK,
            PREVIOUS_EN_PASSANT_SQUARE_INDEX_MASK,
            NEXT_EN_PASSANT_SQUARE_INDEX_MASK,
            PROMOTION_PIECE_MASK,
            NOT_USED_MASK
    };

    @Test
    public void testMaskOverlap() {
        for (final long c1 : MASKS) {
            for (final long c2 : MASKS) {
                if (c1 == c2) {
                    continue;
                }

                assertEquals(0L, c1 & c2, () -> {
                    final String c1String = Long.toString(c1, 16);
                    final String c2String = Long.toString(c2, 16);

                    return c1String + " vs " + c2String;
                });
            }
        }
    }

    @Test
    public void testMaskCoverage() {
        final long masks = Arrays.stream(MASKS).reduce((l1, l2) -> l1 | l2).getAsLong();

        assertEquals(0xFFFFFFFFFFFFFFFFL, masks | NOT_USED_MASK);
    }

    @Test
    public void testIndices() {
        assertEquals(A1, Square.A1.getBitboardIndex());
        assertEquals(A2, Square.A2.getBitboardIndex());
        assertEquals(A3, Square.A3.getBitboardIndex());
        assertEquals(A4, Square.A4.getBitboardIndex());
        assertEquals(A5, Square.A5.getBitboardIndex());
        assertEquals(A6, Square.A6.getBitboardIndex());
        assertEquals(A7, Square.A7.getBitboardIndex());
        assertEquals(A8, Square.A8.getBitboardIndex());
        assertEquals(B1, Square.B1.getBitboardIndex());
        assertEquals(B2, Square.B2.getBitboardIndex());
        assertEquals(B3, Square.B3.getBitboardIndex());
        assertEquals(B4, Square.B4.getBitboardIndex());
        assertEquals(B5, Square.B5.getBitboardIndex());
        assertEquals(B6, Square.B6.getBitboardIndex());
        assertEquals(B7, Square.B7.getBitboardIndex());
        assertEquals(B8, Square.B8.getBitboardIndex());
        assertEquals(C1, Square.C1.getBitboardIndex());
        assertEquals(C2, Square.C2.getBitboardIndex());
        assertEquals(C3, Square.C3.getBitboardIndex());
        assertEquals(C4, Square.C4.getBitboardIndex());
        assertEquals(C5, Square.C5.getBitboardIndex());
        assertEquals(C6, Square.C6.getBitboardIndex());
        assertEquals(C7, Square.C7.getBitboardIndex());
        assertEquals(C8, Square.C8.getBitboardIndex());
        assertEquals(D1, Square.D1.getBitboardIndex());
        assertEquals(D2, Square.D2.getBitboardIndex());
        assertEquals(D3, Square.D3.getBitboardIndex());
        assertEquals(D4, Square.D4.getBitboardIndex());
        assertEquals(D5, Square.D5.getBitboardIndex());
        assertEquals(D6, Square.D6.getBitboardIndex());
        assertEquals(D7, Square.D7.getBitboardIndex());
        assertEquals(D8, Square.D8.getBitboardIndex());
        assertEquals(E1, Square.E1.getBitboardIndex());
        assertEquals(E2, Square.E2.getBitboardIndex());
        assertEquals(E3, Square.E3.getBitboardIndex());
        assertEquals(E4, Square.E4.getBitboardIndex());
        assertEquals(E5, Square.E5.getBitboardIndex());
        assertEquals(E6, Square.E6.getBitboardIndex());
        assertEquals(E7, Square.E7.getBitboardIndex());
        assertEquals(E8, Square.E8.getBitboardIndex());
        assertEquals(F1, Square.F1.getBitboardIndex());
        assertEquals(F2, Square.F2.getBitboardIndex());
        assertEquals(F3, Square.F3.getBitboardIndex());
        assertEquals(F4, Square.F4.getBitboardIndex());
        assertEquals(F5, Square.F5.getBitboardIndex());
        assertEquals(F6, Square.F6.getBitboardIndex());
        assertEquals(F7, Square.F7.getBitboardIndex());
        assertEquals(F8, Square.F8.getBitboardIndex());
        assertEquals(G1, Square.G1.getBitboardIndex());
        assertEquals(G2, Square.G2.getBitboardIndex());
        assertEquals(G3, Square.G3.getBitboardIndex());
        assertEquals(G4, Square.G4.getBitboardIndex());
        assertEquals(G5, Square.G5.getBitboardIndex());
        assertEquals(G6, Square.G6.getBitboardIndex());
        assertEquals(G7, Square.G7.getBitboardIndex());
        assertEquals(G8, Square.G8.getBitboardIndex());
        assertEquals(H1, Square.H1.getBitboardIndex());
        assertEquals(H2, Square.H2.getBitboardIndex());
        assertEquals(H3, Square.H3.getBitboardIndex());
        assertEquals(H4, Square.H4.getBitboardIndex());
        assertEquals(H5, Square.H5.getBitboardIndex());
        assertEquals(H6, Square.H6.getBitboardIndex());
        assertEquals(H7, Square.H7.getBitboardIndex());
        assertEquals(H8, Square.H8.getBitboardIndex());
    }
}