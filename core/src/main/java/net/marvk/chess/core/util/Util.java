package net.marvk.chess.core.util;

import net.marvk.chess.core.board.Piece;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Util {
    private Util() {
        throw new AssertionError("No instances of utility class " + Util.class);
    }

    public static final Map<Piece, Double> SCORES;

    static {
        final Map<Piece, Double> map = new EnumMap<>(Piece.class);

        map.put(Piece.KING, 0.);
        map.put(Piece.QUEEN, 9.);
        map.put(Piece.ROOK, 5.);
        map.put(Piece.BISHOP, 3.5);
        map.put(Piece.KNIGHT, 3.);
        map.put(Piece.PAWN, 1.);

        SCORES = Collections.unmodifiableMap(map);
    }

    public static int nodesPerSecond(final Duration duration, final int nodes) {
        return (int) Math.round(((double) nodes / duration.toNanos()) * TimeUnit.SECONDS.toNanos(1));
    }
}
