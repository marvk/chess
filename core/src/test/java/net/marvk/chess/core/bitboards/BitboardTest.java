package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Square;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

class BitboardTest {

    @Test
    public void testPredefinedRookMagics() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        final long[] loadedMagics = getMagics(Bitboard.ROOK);
        final long[] calculatedMagics = getMagics(getBitboardInstance(Configuration::rookConfiguration));

        Assertions.assertArrayEquals(loadedMagics, calculatedMagics);
    }

    @Test
    public void testPredefinedBishopMagics() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        final long[] loadedMagics = getMagics(Bitboard.BISHOP);
        final long[] calculatedMagics = getMagics(getBitboardInstance(Configuration::bishopConfiguration));

        Assertions.assertArrayEquals(loadedMagics, calculatedMagics);
    }

    private static Bitboard getBitboardInstance(final Function<Square, Configuration> configuration) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor<Bitboard> constructor = Bitboard.class.getDeclaredConstructor(Function.class);

        constructor.setAccessible(true);

        return constructor.newInstance(configuration);
    }

    private static long[] getMagics(final Bitboard bitboard) throws NoSuchFieldException, IllegalAccessException {
        final Field field = Bitboard.class.getDeclaredField("magics");
        field.setAccessible(true);
        return (long[]) field.get(bitboard);
    }
}