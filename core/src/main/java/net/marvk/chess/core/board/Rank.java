package net.marvk.chess.core.board;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Rank {
    RANK_1('1', 0),
    RANK_2('2', 1),
    RANK_3('3', 2),
    RANK_4('4', 3),
    RANK_5('5', 4),
    RANK_6('6', 5),
    RANK_7('7', 6),
    RANK_8('8', 7);

    private static final Map<Character, Rank> FEN_RANK_MAP = Arrays.stream(Rank.values())
                                                                   .collect(Collectors.toMap(Rank::getFen, Function.identity()));

    private static final Map<Integer, Rank> INDEX_RANK_MAP = Arrays.stream(values())
                                                                   .collect(Collectors.collectingAndThen(Collectors.toMap(Rank::getIndex, Function
                                                                           .identity()), Collections::unmodifiableMap));

    private final char fen;
    private final int index;

    Rank(final char fen, final int index) {
        this.fen = fen;
        this.index = index;
    }

    public char getFen() {
        return fen;
    }

    public int getIndex() {
        return index;
    }

    public Rank translate(final Direction direction) {
        return INDEX_RANK_MAP.get(index + direction.getRankDifference());
    }

    public static Rank getRankFromFen(final Character fen) {
        return FEN_RANK_MAP.get(fen);
    }
}
