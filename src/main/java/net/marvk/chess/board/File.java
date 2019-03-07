package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum File {
    FILE_A('a', 0),
    FILE_B('b', 1),
    FILE_C('c', 2),
    FILE_D('d', 3),
    FILE_E('e', 4),
    FILE_F('f', 5),
    FILE_G('g', 6),
    FILE_H('h', 7);

    private static final Map<Character, File> FEN_FILE_MAP = Arrays.stream(File.values())
                                                                   .collect(Collectors.toMap(File::getFen, Function.identity()));

    private static final Map<Integer, File> INDEX_FILE_MAP = Arrays.stream(values())
                                                                   .collect(Collectors.collectingAndThen(Collectors.toMap(File::getIndex, Function
                                                                           .identity()), Collections::unmodifiableMap));

    private final char fen;
    private final int index;

    File(final char fen, final int index) {
        this.fen = fen;
        this.index = index;
    }

    public char getFen() {
        return fen;
    }

    public int getIndex() {
        return index;
    }

    public File translate(final Direction direction) {
        return INDEX_FILE_MAP.get(index + direction.getFileDifference());
    }

    public static File getFileFromFen(final Character fen) {
        return FEN_FILE_MAP.get(fen);
    }
}
