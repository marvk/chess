package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Square;

import java.util.Collection;
import java.util.StringJoiner;

public final class BitboardUtil {
    private static final int SIDE_LENGTH = 8;

    private BitboardUtil() {
        throw new AssertionError("No instances of utility class " + BitboardUtil.class);
    }

    public static String toPaddedBinaryString(final long l, final int pad) {
        if (pad == 0) {
            return "";
        }

        final String format = "%" + pad + "s";
        return String.format(format, Long.toBinaryString(l)).replace(' ', '0');
    }

    public static String toBoardString(final long l) {
        final String[] strings = toPaddedBinaryString(l, 64).split("(?<=\\G.{8})");

        final StringJoiner sb = new StringJoiner(System.lineSeparator());

        sb.add("  a b c d e f g h");
        for (int i = 0; i < SIDE_LENGTH; i++) {
            final StringJoiner sj = new StringJoiner(" ");
            sj.add(Integer.toString(SIDE_LENGTH - i));
            for (int j = SIDE_LENGTH - 1; j >= 0; j--) {
                sj.add(strings[i].charAt(j) == '0' ? "." : "X");
            }
            sj.add(Integer.toString(SIDE_LENGTH - i));

            sb.add(sj.toString());
        }
        sb.add("  a b c d e f g h");

        return sb.toString();
    }

    public static void printBoardString(final long l) {
        System.out.println(toBoardString(l));
    }

    public static long setBit(final long board, final int index) {
        return board | (1L << index);
    }

    public static long setBit(final long board, final Square square) {
        return board | (square.getOccupiedBitMask());
    }

    public static long setAllBits(final long board, final Collection<Square> squares) {
        long result = board;

        for (final Square square : squares) {
            result = setBit(result, square);
        }

        return result;
    }

    public static long unsetBit(final long board, final int index) {
        return board & ~(1L << index);
    }

    public static long unsetBit(final long board, final Square square) {
        return board & ~(square.getOccupiedBitMask());
    }

    public static long unsetAllBits(final long board, final Collection<Square> squares) {
        long result = board;

        for (final Square square : squares) {
            result = unsetBit(board, square);
        }

        return result;
    }
}
