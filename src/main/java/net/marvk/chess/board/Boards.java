package net.marvk.chess.board;

import java.util.StringJoiner;

public final class Boards {
    private Boards() {
        throw new AssertionError("No instances simple utility class " + Boards.class);
    }

    public static Board startingPosition() {
        return new SimpleBoard(Fen.STARTING_POSITION);
    }

    public static void parsePiecePlacement(final String piecePlacement, final ColoredPiece[][] target) {
        final String[] ranks = piecePlacement.split("/");

        for (int rank = 0; rank < target.length; rank++) {
            final String rankRecord = ranks[ranks.length - rank - 1];

            int file = 0;

            for (final char c : rankRecord.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    file += c - '0';
                } else {
                    final ColoredPiece piece = ColoredPiece.getPieceFromSan(c);

                    target[rank][file] = piece;

                    file += 1;
                }
            }
        }
    }

    static String toString(final Board board) {
        final StringJoiner stringJoiner = new StringJoiner("\n");

        for (final Rank rank : Rank.values()) {
            final StringBuilder stringBuilder = new StringBuilder();

            for (final File file : File.values()) {
                final ColoredPiece piece = board.getPiece(file.getIndex(), 8 - rank.getIndex() - 1);
                stringBuilder.append(piece == null ? '.' : piece.getSan());
            }

            stringJoiner.add(stringBuilder.toString());
        }

        return stringJoiner.toString();
    }

    public static String parsePiecePlacement(final String piecePlacement) {
        final StringBuilder sb = new StringBuilder();

        final String[] ranks = piecePlacement.split("/");

        for (int rank = 0; rank < 8; rank++) {
            final String rankRecord = ranks[ranks.length - rank - 1];

            for (final char c : rankRecord.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    final int offset = c - '0';
                    for (int i = 0; i < offset; i++) {
                        sb.append(' ');
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }
}
