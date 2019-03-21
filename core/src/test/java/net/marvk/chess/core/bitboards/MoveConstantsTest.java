package net.marvk.chess.core.bitboards;

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
}