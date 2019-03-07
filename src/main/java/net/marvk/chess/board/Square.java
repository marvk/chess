package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Square {
    A1(File.FILE_A, Rank.RANK_1), A2(File.FILE_A, Rank.RANK_2), A3(File.FILE_A, Rank.RANK_3), A4(File.FILE_A, Rank.RANK_4), A5(File.FILE_A, Rank.RANK_5), A6(File.FILE_A, Rank.RANK_6), A7(File.FILE_A, Rank.RANK_7), A8(File.FILE_A, Rank.RANK_8),
    B1(File.FILE_B, Rank.RANK_1), B2(File.FILE_B, Rank.RANK_2), B3(File.FILE_B, Rank.RANK_3), B4(File.FILE_B, Rank.RANK_4), B5(File.FILE_B, Rank.RANK_5), B6(File.FILE_B, Rank.RANK_6), B7(File.FILE_B, Rank.RANK_7), B8(File.FILE_B, Rank.RANK_8),
    C1(File.FILE_C, Rank.RANK_1), C2(File.FILE_C, Rank.RANK_2), C3(File.FILE_C, Rank.RANK_3), C4(File.FILE_C, Rank.RANK_4), C5(File.FILE_C, Rank.RANK_5), C6(File.FILE_C, Rank.RANK_6), C7(File.FILE_C, Rank.RANK_7), C8(File.FILE_C, Rank.RANK_8),
    D1(File.FILE_D, Rank.RANK_1), D2(File.FILE_D, Rank.RANK_2), D3(File.FILE_D, Rank.RANK_3), D4(File.FILE_D, Rank.RANK_4), D5(File.FILE_D, Rank.RANK_5), D6(File.FILE_D, Rank.RANK_6), D7(File.FILE_D, Rank.RANK_7), D8(File.FILE_D, Rank.RANK_8),
    E1(File.FILE_E, Rank.RANK_1), E2(File.FILE_E, Rank.RANK_2), E3(File.FILE_E, Rank.RANK_3), E4(File.FILE_E, Rank.RANK_4), E5(File.FILE_E, Rank.RANK_5), E6(File.FILE_E, Rank.RANK_6), E7(File.FILE_E, Rank.RANK_7), E8(File.FILE_E, Rank.RANK_8),
    F1(File.FILE_F, Rank.RANK_1), F2(File.FILE_F, Rank.RANK_2), F3(File.FILE_F, Rank.RANK_3), F4(File.FILE_F, Rank.RANK_4), F5(File.FILE_F, Rank.RANK_5), F6(File.FILE_F, Rank.RANK_6), F7(File.FILE_F, Rank.RANK_7), F8(File.FILE_F, Rank.RANK_8),
    G1(File.FILE_G, Rank.RANK_1), G2(File.FILE_G, Rank.RANK_2), G3(File.FILE_G, Rank.RANK_3), G4(File.FILE_G, Rank.RANK_4), G5(File.FILE_G, Rank.RANK_5), G6(File.FILE_G, Rank.RANK_6), G7(File.FILE_G, Rank.RANK_7), G8(File.FILE_G, Rank.RANK_8),
    H1(File.FILE_H, Rank.RANK_1), H2(File.FILE_H, Rank.RANK_2), H3(File.FILE_H, Rank.RANK_3), H4(File.FILE_H, Rank.RANK_4), H5(File.FILE_H, Rank.RANK_5), H6(File.FILE_H, Rank.RANK_6), H7(File.FILE_H, Rank.RANK_7), H8(File.FILE_H, Rank.RANK_8);

    private static final Map<File, Map<Rank, Square>> FILE_RANK_SQUARE_MAP =
            Arrays.stream(Square.values())
                  .collect(Collectors.groupingBy(
                          Square::getFile,
                          Collectors.toMap(Square::getRank, Function.identity())
                          )
                  );

    private static final Map<Integer, Map<Integer, Square>> INDEX_FILE_RANK_SQUARE_MAP =
            Arrays.stream(Square.values())
                  .collect(Collectors.groupingBy(
                          square -> square.getFile().getIndex(),
                          Collectors.toMap(square -> square.getRank().getIndex(), Function.identity())
                          )
                  );

    private final File file;
    private final Rank rank;

    Square(final File file, final Rank rank) {
        this.file = file;
        this.rank = rank;
    }

    public static Square getSquareFromFen(final String fen) {
        if ("-".equals(fen)) {
            return null;
        }

        final char file = fen.charAt(0);
        final char rank = fen.charAt(1);

        return get(File.getFileFromFen(file), Rank.getRankFromFen(rank));
    }

    public static Square get(final File file, final Rank rank) {
        return get(rank.getIndex(), file.getIndex());
    }

    public static Square get(final int file, final int rank) {
        final Map<Integer, Square> fileSquareMap = INDEX_FILE_RANK_SQUARE_MAP.get(rank);

        if (fileSquareMap == null) {
            return null;
        }

        return fileSquareMap.get(file);
    }

    public Square translate(final Direction direction) {
        final File translatedFile = file.translate(direction);
        final Rank translatedRank = rank.translate(direction);

        if (translatedRank == null || translatedFile == null) {
            return null;
        }

        return get(translatedFile, translatedRank);
    }

    public Rank getRank() {
        return rank;
    }

    public File getFile() {
        return file;
    }
}
