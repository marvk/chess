package net.marvk.chess.core.bitboards;

/*
 * MSB . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . LSB
 *
 * xxxxxxxxxxxxxxx xxxxxx xxxxxx xxxxxxxxxxxx x  xxxxxx xxxxxx x x x x x x xxx xxx
 *                    |       |         |     |     |      |   | | | | | |  |   |
 *                    |       |         |     |     |      |   | | | | | |  |   |
 * _____UNUSED____    |       |         |     |     |      |   | | | | | |  |    --> Piece moved
 *                    |       |         |     |     |      |   | | | | | |  |
 *                    |       |         |     |     |      |   | | | | | |   ------> Piece attacked
 *                    |       |         |     |     |      |   | | | | | |
 *                    |       |         |     |     |      |   | | | | |  ---------> Self lost king side castle
 *                    |       |         |     |     |      |   | | | | |
 *                    |       |         |     |     |      |   | | | |  -----------> Self lost queen side castle
 *                    |       |         |     |     |      |   | | | |
 *                    |       |         |     |     |      |   | | |  -------------> Opponent lost king side castle
 *                    |       |         |     |     |      |   | | |
 *                    |       |         |     |     |      |   | |  ---------------> Opponent lost queen side castle
 *                    |       |         |     |     |      |   | |
 *                    |       |         |     |     |      |   |  -----------------> Is castle move
 *                    |       |         |     |     |      |   |
 *                    |       |         |     |     |      |    -------------------> Is en passant attack
 *                    |       |         |     |     |      |
 *                    |       |         |     |     |       -----------------------> Source square
 *                    |       |         |     |     |
 *                    |       |         |     |      ------------------------------> Target square
 *                    |       |         |     |
 *                    |       |         |      ------------------------------------> Halfmove reset
 *                    |       |         |
 *                    |       |          ------------------------------------------> Previous halfmove
 *                    |       |
 *                    |         ---------------------------------------------------> Previous en passant square } Technically you can use files
 *                    |
 *                     ------------------------------------------------------------> Next en passant square     } and get that information from the pawn move
 */
public final class MoveConstants {
    public static final int NO_PIECE = 0;
    public static final int PAWN     = 0b001;
    public static final int KNIGHT   = 0b010;
    public static final int BISHOP   = 0b011;
    public static final int ROOK     = 0b100;
    public static final int QUEEN    = 0b101;
    public static final int KING     = 0b110;

    public static final long PIECE_MOVED_MASK                      = 0x7L;
    public static final long PIECE_ATTACKED_MASK                   = 0x38L;
    public static final long SELF_LOST_KING_SIDE_CASTLE_MASK       = 0x40L;
    public static final long SELF_LOST_QUEEN_SIDE_CASTLE_MASK      = 0x80L;
    public static final long OPPONENT_LOST_KING_SIDE_CASTLE_MASK   = 0x100L;
    public static final long OPPONENT_LOST_QUEEN_SIDE_CASTLE_MASK  = 0x200L;
    public static final long CASTLE_MOVE_MASK                      = 0x400L;
    public static final long EN_PASSANT_ATTACK_MASK                = 0x800L;
    public static final long SOURCE_SQUARE_INDEX_MASK              = 0x3f000L;
    public static final long TARGET_SQUARE_INDEX_MASK              = 0xfc0000L;
    public static final long HALFMOVE_RESET_MASK                   = 0x1000000L;
    public static final long PREVIOUS_HALFMOVE_MASK                = 0x1ffe000000L;
    public static final long PREVIOUS_EN_PASSANT_SQUARE_INDEX_MASK = 0x7e000000000L;
    public static final long NEXT_EN_PASSANT_SQUARE_INDEX_MASK     = 0x1f80000000000L;
    public static final long PROMOTION_PIECE_MASK                  = 0xe000000000000L;
    public static final long NOT_USED_MASK                         = 0xfff0000000000000L;

    public static final int PIECE_MOVED_SHIFT                      = Long.numberOfTrailingZeros(PIECE_MOVED_MASK);
    public static final int PIECE_ATTACKED_SHIFT                   = Long.numberOfTrailingZeros(PIECE_ATTACKED_MASK);
    public static final int SELF_LOST_KING_SIDE_CASTLE_SHIFT       = Long.numberOfTrailingZeros(SELF_LOST_KING_SIDE_CASTLE_MASK);
    public static final int SELF_LOST_QUEEN_SIDE_CASTLE_SHIFT      = Long.numberOfTrailingZeros(SELF_LOST_QUEEN_SIDE_CASTLE_MASK);
    public static final int OPPONENT_LOST_KING_SIDE_CASTLE_SHIFT   = Long.numberOfTrailingZeros(OPPONENT_LOST_KING_SIDE_CASTLE_MASK);
    public static final int OPPONENT_LOST_QUEEN_SIDE_CASTLE_SHIFT  = Long.numberOfTrailingZeros(OPPONENT_LOST_QUEEN_SIDE_CASTLE_MASK);
    public static final int CASTLE_MOVE_SHIFT                      = Long.numberOfTrailingZeros(CASTLE_MOVE_MASK);
    public static final int EN_PASSANT_ATTACK_SHIFT                = Long.numberOfTrailingZeros(EN_PASSANT_ATTACK_MASK);
    public static final int SOURCE_SQUARE_INDEX_SHIFT              = Long.numberOfTrailingZeros(SOURCE_SQUARE_INDEX_MASK);
    public static final int TARGET_SQUARE_INDEX_SHIFT              = Long.numberOfTrailingZeros(TARGET_SQUARE_INDEX_MASK);
    public static final int HALFMOVE_RESET_SHIFT                   = Long.numberOfTrailingZeros(HALFMOVE_RESET_MASK);
    public static final int PREVIOUS_HALFMOVE_SHIFT                = Long.numberOfTrailingZeros(PREVIOUS_HALFMOVE_MASK);
    public static final int PREVIOUS_EN_PASSANT_SQUARE_INDEX_SHIFT = Long.numberOfTrailingZeros(PREVIOUS_EN_PASSANT_SQUARE_INDEX_MASK);
    public static final int NEXT_EN_PASSANT_SQUARE_INDEX_SHIFT     = Long.numberOfTrailingZeros(NEXT_EN_PASSANT_SQUARE_INDEX_MASK);
    public static final int PROMOTION_PIECE_SHIFT                  = Long.numberOfTrailingZeros(PROMOTION_PIECE_MASK);
    public static final int NOT_USED_SHIFT                         = Long.numberOfTrailingZeros(NOT_USED_MASK);

    private MoveConstants() {
        throw new AssertionError("No instances of utility class " + MoveConstants.class);
    }
}
