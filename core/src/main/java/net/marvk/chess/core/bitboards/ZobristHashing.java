package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.ColoredPiece;
import net.marvk.chess.core.board.Square;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class ZobristHashing {
    private static final long[][] SQUARE_PIECE_HASHES;
    private static final long[] EN_PASSANT_HASHES;
    private static final long WHITE_KING_CASTLE_HASH;
    private static final long WHITE_QUEEN_CASTLE_HASH;
    private static final long BLACK_KING_CASTLE_HASH;
    private static final long BLACK_QUEEN_CASTLE_HASH;

    static {
        final Random random = new Random(350);

        SQUARE_PIECE_HASHES = new long[64][12];

        final Set<Long> hashes = new HashSet<>();

        for (final Square square : Square.values()) {
            final long[] current = SQUARE_PIECE_HASHES[square.getBitboardIndex()];

            for (final ColoredPiece value : ColoredPiece.values()) {
                current[value.ordinal()] = findNewHash(random, hashes);
            }
        }

        EN_PASSANT_HASHES = new long[64];

        for (final Square value : Square.values()) {
            EN_PASSANT_HASHES[value.getBitboardIndex()] = findNewHash(random, hashes);
        }

        WHITE_KING_CASTLE_HASH = findNewHash(random, hashes);
        WHITE_QUEEN_CASTLE_HASH = findNewHash(random, hashes);
        BLACK_KING_CASTLE_HASH = findNewHash(random, hashes);
        BLACK_QUEEN_CASTLE_HASH = findNewHash(random, hashes);
    }

    private static long findNewHash(final Random random, final Set<Long> hashes) {
        while (true) {
            final long hash = random.nextLong();

            if (hashes.add(hash)) {
                return hash;
            }
        }
    }

    private ZobristHashing() {
        throw new AssertionError("No instances of utility class " + ZobristHashing.class);
    }

    public static long hashPieceSquare(final ColoredPiece coloredPiece, final int squareIndex) {
        return SQUARE_PIECE_HASHES[squareIndex][coloredPiece.ordinal()];
    }

    public static long hashEnPassant(final int squareIndex) {
        return EN_PASSANT_HASHES[squareIndex];
    }

    public static long whiteKingCastleHash() {
        return WHITE_KING_CASTLE_HASH;
    }

    public static long whiteQueenCastleHash() {
        return WHITE_QUEEN_CASTLE_HASH;
    }

    public static long blackKingCastleHash() {
        return BLACK_KING_CASTLE_HASH;
    }

    public static long blackQueenCastleHash() {
        return BLACK_QUEEN_CASTLE_HASH;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.deepToString(SQUARE_PIECE_HASHES));
    }
}
