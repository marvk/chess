package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum File {
    FILE_1('1', 0),
    FILE_2('2', 1),
    FILE_3('3', 2),
    FILE_4('4', 3),
    FILE_5('5', 4),
    FILE_6('6', 5),
    FILE_7('7', 6),
    FILE_8('8', 7);

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
