package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Square {
    A1(Rank.RANK_A, File.FILE_1), A2(Rank.RANK_A, File.FILE_2), A3(Rank.RANK_A, File.FILE_3), A4(Rank.RANK_A, File.FILE_4), A5(Rank.RANK_A, File.FILE_5), A6(Rank.RANK_A, File.FILE_6), A7(Rank.RANK_A, File.FILE_7), A8(Rank.RANK_A, File.FILE_8),
    B1(Rank.RANK_B, File.FILE_1), B2(Rank.RANK_B, File.FILE_2), B3(Rank.RANK_B, File.FILE_3), B4(Rank.RANK_B, File.FILE_4), B5(Rank.RANK_B, File.FILE_5), B6(Rank.RANK_B, File.FILE_6), B7(Rank.RANK_B, File.FILE_7), B8(Rank.RANK_B, File.FILE_8),
    C1(Rank.RANK_C, File.FILE_1), C2(Rank.RANK_C, File.FILE_2), C3(Rank.RANK_C, File.FILE_3), C4(Rank.RANK_C, File.FILE_4), C5(Rank.RANK_C, File.FILE_5), C6(Rank.RANK_C, File.FILE_6), C7(Rank.RANK_C, File.FILE_7), C8(Rank.RANK_C, File.FILE_8),
    D1(Rank.RANK_D, File.FILE_1), D2(Rank.RANK_D, File.FILE_2), D3(Rank.RANK_D, File.FILE_3), D4(Rank.RANK_D, File.FILE_4), D5(Rank.RANK_D, File.FILE_5), D6(Rank.RANK_D, File.FILE_6), D7(Rank.RANK_D, File.FILE_7), D8(Rank.RANK_D, File.FILE_8),
    E1(Rank.RANK_E, File.FILE_1), E2(Rank.RANK_E, File.FILE_2), E3(Rank.RANK_E, File.FILE_3), E4(Rank.RANK_E, File.FILE_4), E5(Rank.RANK_E, File.FILE_5), E6(Rank.RANK_E, File.FILE_6), E7(Rank.RANK_E, File.FILE_7), E8(Rank.RANK_E, File.FILE_8),
    F1(Rank.RANK_F, File.FILE_1), F2(Rank.RANK_F, File.FILE_2), F3(Rank.RANK_F, File.FILE_3), F4(Rank.RANK_F, File.FILE_4), F5(Rank.RANK_F, File.FILE_5), F6(Rank.RANK_F, File.FILE_6), F7(Rank.RANK_F, File.FILE_7), F8(Rank.RANK_F, File.FILE_8),
    G1(Rank.RANK_G, File.FILE_1), G2(Rank.RANK_G, File.FILE_2), G3(Rank.RANK_G, File.FILE_3), G4(Rank.RANK_G, File.FILE_4), G5(Rank.RANK_G, File.FILE_5), G6(Rank.RANK_G, File.FILE_6), G7(Rank.RANK_G, File.FILE_7), G8(Rank.RANK_G, File.FILE_8),
    H1(Rank.RANK_H, File.FILE_1), H2(Rank.RANK_H, File.FILE_2), H3(Rank.RANK_H, File.FILE_3), H4(Rank.RANK_H, File.FILE_4), H5(Rank.RANK_H, File.FILE_5), H6(Rank.RANK_H, File.FILE_6), H7(Rank.RANK_H, File.FILE_7), H8(Rank.RANK_H, File.FILE_8);

    private static final Map<Rank, Map<File, Square>> RANK_FILE_SQUARE_MAP =
            Arrays.stream(Square.values())
                  .collect(Collectors.groupingBy(
                          Square::getRank,
                          Collectors.toMap(Square::getFile, Function.identity())
                          )
                  );

    private final Rank rank;
    private final File file;

    Square(final Rank rank, final File file) {
        this.rank = rank;
        this.file = file;
    }

    public static Square getSquareFromFen(final String fen) {
        if ("-".equals(fen)) {
            return null;
        }

        final char rank = fen.charAt(0);
        final char file = fen.charAt(1);

        return RANK_FILE_SQUARE_MAP.get(Rank.getRankFromfen(rank)).get(File.getFileFromFen(file));
    }

    public Rank getRank() {
        return rank;
    }

    public File getFile() {
        return file;
    }
}
