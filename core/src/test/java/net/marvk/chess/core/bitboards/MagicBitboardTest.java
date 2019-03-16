package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Square;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

class MagicBitboardTest {

    @Test
    public void testPredefinedRookMagics() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        final long[] loadedMagics = getMagics(MagicBitboard.ROOK);
        final long[] calculatedMagics = getMagics(getBitboardInstance(Configuration::rookConfiguration));

        Assertions.assertArrayEquals(loadedMagics, calculatedMagics);
    }

    @Test
    public void testPredefinedBishopMagics() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        final long[] loadedMagics = getMagics(MagicBitboard.BISHOP);
        final long[] calculatedMagics = getMagics(getBitboardInstance(Configuration::bishopConfiguration));

        Assertions.assertArrayEquals(loadedMagics, calculatedMagics);
    }

    private static MagicBitboard getBitboardInstance(final Function<Square, Configuration> configuration) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor<MagicBitboard> constructor = MagicBitboard.class.getDeclaredConstructor(Function.class);

        constructor.setAccessible(true);

        return constructor.newInstance(configuration);
    }

    private static long[] getMagics(final MagicBitboard bitboard) throws NoSuchFieldException, IllegalAccessException {
        final Field field = MagicBitboard.class.getDeclaredField("magics");
        field.setAccessible(true);
        return (long[]) field.get(bitboard);
    }
}