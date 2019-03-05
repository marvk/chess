package net.marvk.chess.board;

public class Board {
    private static final int LENGTH = 8;
    private final ColoredPiece[][] board;
    private BoardState boardState;

    public Board() {
        board = new ColoredPiece[LENGTH][LENGTH];
    }

    public Board(final String san) {
        board = new ColoredPiece[LENGTH][LENGTH];

        final String[] split = san.split(" ");

        boardState = new BoardState(split);

        final String[] ranks = split[0].split("/");

        for (int rank = 0; rank < LENGTH; rank++) {
            final String rankRecord = ranks[rank];

            int file = 0;

            for (final char c : rankRecord.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    file += c - '0';
                } else {
                    final ColoredPiece piece = ColoredPiece.getPieceFromSan(c);

                    board[rank][file] = piece;

                    file += 1;
                }
            }
        }
    }

    public ColoredPiece getPiece(final Square square) {
        return getPiece(square.getFile().getIndex(), square.getRank().getIndex());
    }

    public ColoredPiece getPiece(final int rank, final int file) {
        return board[rank][file];
    }

    public static void main(String[] args) {
        final Board board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                final ColoredPiece piece = board.getPiece(i, j);
                System.out.print(piece == null ? '.' : piece.getSan());
            }
            System.out.println();
        }
    }
}
