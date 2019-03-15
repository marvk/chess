package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Square;

import java.util.Arrays;
import java.util.function.Function;

public final class Bitboard {
    public static final Bitboard ROOK;
    public static final Bitboard BISHOP;

    private static final Square[] SQUARES = Square.values();

    static {
        ROOK = new Bitboard(Configuration::rookConfiguration);
        BISHOP = new Bitboard(Configuration::bishopConfiguration);
    }

    private final long[] masks = new long[64];
    private final long[] magics = new long[64];
    private final long[] hashShifts = new long[64];
    private final int[] hashMasks = new int[64];

    private final long[][] attacks = new long[64][];

    private Bitboard(final Function<Square, Configuration> configurationGenerator) {
        for (final Square square : SQUARES) {
            final Configuration configuration = configurationGenerator.apply(square);

            final int index = square.getBitboardIndex();

            magics[index] = configuration.getMagic();
            masks[index] = configuration.getMask();
            hashShifts[index] = configuration.getHashShift();
            hashMasks[index] = configuration.getHashMask();

            attacks[index] = configuration.generateAllAttacks();
        }
    }

    public long attacks(final long opponentOccupancy, final Square square) {
        return attacks[square.getBitboardIndex()][hash(opponentOccupancy, square.getBitboardIndex())];
    }

    private int hash(final long l, final int squareIndex) {
        return ((int) ((((l & masks[squareIndex]) * magics[squareIndex]) >> hashShifts[squareIndex]) & hashMasks[squareIndex]));
    }

    public static void main(String[] args) {
        final long opponentOccupancy = MagicBitboards.setAllBits(0L, Arrays.asList(Square.H1, Square.E1));

        System.out.println(opponentOccupancy);

        System.out.println(MagicBitboards.toBoardString(opponentOccupancy));

        final long attacks = ROOK.attacks(opponentOccupancy, Square.A1);

        System.out.println(MagicBitboards.toBoardString(attacks));
    }
}
