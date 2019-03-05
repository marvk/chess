package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Rank {
    RANK_A('a', 0),
    RANK_B('b', 1),
    RANK_C('c', 2),
    RANK_D('d', 3),
    RANK_E('e', 4),
    RANK_F('f', 5),
    RANK_G('g', 6),
    RANK_H('h', 7);

    private static final Map<Character, Rank> FEN_RANK_MAP = Arrays.stream(Rank.values())
                                                                   .collect(Collectors.toMap(Rank::getFen, Function.identity()));
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

    public static Rank getRankFromfen(final Character fen) {
        return FEN_RANK_MAP.get(fen);
    }
}
