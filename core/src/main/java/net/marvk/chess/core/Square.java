package net.marvk.chess.core;

import lombok.extern.log4j.Log4j2;

import java.util.EnumMap;

@Log4j2
public enum Square {
    A1(File.FILE_A, Rank.RANK_1), A2(File.FILE_A, Rank.RANK_2), A3(File.FILE_A, Rank.RANK_3), A4(File.FILE_A, Rank.RANK_4), A5(File.FILE_A, Rank.RANK_5), A6(File.FILE_A, Rank.RANK_6), A7(File.FILE_A, Rank.RANK_7), A8(File.FILE_A, Rank.RANK_8),
    B1(File.FILE_B, Rank.RANK_1), B2(File.FILE_B, Rank.RANK_2), B3(File.FILE_B, Rank.RANK_3), B4(File.FILE_B, Rank.RANK_4), B5(File.FILE_B, Rank.RANK_5), B6(File.FILE_B, Rank.RANK_6), B7(File.FILE_B, Rank.RANK_7), B8(File.FILE_B, Rank.RANK_8),
    C1(File.FILE_C, Rank.RANK_1), C2(File.FILE_C, Rank.RANK_2), C3(File.FILE_C, Rank.RANK_3), C4(File.FILE_C, Rank.RANK_4), C5(File.FILE_C, Rank.RANK_5), C6(File.FILE_C, Rank.RANK_6), C7(File.FILE_C, Rank.RANK_7), C8(File.FILE_C, Rank.RANK_8),
    D1(File.FILE_D, Rank.RANK_1), D2(File.FILE_D, Rank.RANK_2), D3(File.FILE_D, Rank.RANK_3), D4(File.FILE_D, Rank.RANK_4), D5(File.FILE_D, Rank.RANK_5), D6(File.FILE_D, Rank.RANK_6), D7(File.FILE_D, Rank.RANK_7), D8(File.FILE_D, Rank.RANK_8),
    E1(File.FILE_E, Rank.RANK_1), E2(File.FILE_E, Rank.RANK_2), E3(File.FILE_E, Rank.RANK_3), E4(File.FILE_E, Rank.RANK_4), E5(File.FILE_E, Rank.RANK_5), E6(File.FILE_E, Rank.RANK_6), E7(File.FILE_E, Rank.RANK_7), E8(File.FILE_E, Rank.RANK_8),
    F1(File.FILE_F, Rank.RANK_1), F2(File.FILE_F, Rank.RANK_2), F3(File.FILE_F, Rank.RANK_3), F4(File.FILE_F, Rank.RANK_4), F5(File.FILE_F, Rank.RANK_5), F6(File.FILE_F, Rank.RANK_6), F7(File.FILE_F, Rank.RANK_7), F8(File.FILE_F, Rank.RANK_8),
    G1(File.FILE_G, Rank.RANK_1), G2(File.FILE_G, Rank.RANK_2), G3(File.FILE_G, Rank.RANK_3), G4(File.FILE_G, Rank.RANK_4), G5(File.FILE_G, Rank.RANK_5), G6(File.FILE_G, Rank.RANK_6), G7(File.FILE_G, Rank.RANK_7), G8(File.FILE_G, Rank.RANK_8),
    H1(File.FILE_H, Rank.RANK_1), H2(File.FILE_H, Rank.RANK_2), H3(File.FILE_H, Rank.RANK_3), H4(File.FILE_H, Rank.RANK_4), H5(File.FILE_H, Rank.RANK_5), H6(File.FILE_H, Rank.RANK_6), H7(File.FILE_H, Rank.RANK_7), H8(File.FILE_H, Rank.RANK_8);

    private final File file;
    private final Rank rank;

    private final int bitboardIndex;
    private final long occupiedBitMask;

    private final boolean northSouthEdgeSquare;
    private final boolean eastWestEdgeSquare;
    private final boolean edgeSquare;

    private static final Square[][] SQUARES;

    static {
        SQUARES = new Square[8][8];

        for (final Square square : values()) {
            SQUARES[square.file.getIndex()][square.rank.getIndex()] = square;
        }

        for (final Square square : values()) {
            for (final Direction value : Direction.values()) {
                square.translateMap.put(value, square.calculateTranslate(value));
            }
        }
    }

    private final EnumMap<Direction, Square> translateMap;

    Square(final File file, final Rank rank) {
        this.file = file;
        this.rank = rank;

        this.bitboardIndex = rank.getIndex() * 8 + file.getIndex();

        this.occupiedBitMask = 1L << bitboardIndex;

        this.translateMap = new EnumMap<>(Direction.class);

        this.northSouthEdgeSquare = rank == Rank.RANK_1 || rank == Rank.RANK_8;
        this.eastWestEdgeSquare = file == File.FILE_A || file == File.FILE_H;

        this.edgeSquare = northSouthEdgeSquare || eastWestEdgeSquare;
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
        return get(file.getIndex(), rank.getIndex());
    }

    public static Square get(final int file, final int rank) {
        return SQUARES[file][rank];
    }

    public Square translate(final Direction direction) {
        return translateMap.get(direction);
    }

    private Square calculateTranslate(final Direction direction) {
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

    public int getBitboardIndex() {
        return bitboardIndex;
    }

    public long getOccupiedBitMask() {
        return occupiedBitMask;
    }

    public String getFen() {
        return Character.toString(file.getFen()) + Character.toString(rank.getFen());
    }

    public boolean isEdgeForDirection(final Direction direction) {
        switch (direction) {
            case NORTH:
            case SOUTH:
                return northSouthEdgeSquare;
            case EAST:
            case WEST:
                return eastWestEdgeSquare;
            case NORTH_EAST:
            case NORTH_WEST:
            case SOUTH_EAST:
            case SOUTH_WEST:
                return edgeSquare;
            default:
                return false;
        }
    }

    public boolean isEdgeSquare() {
        return edgeSquare;
    }

    public boolean isNorthSouthEdge() {
        return edgeSquare;
    }

    public boolean isEastWestEdge() {
        return edgeSquare;
    }
}
