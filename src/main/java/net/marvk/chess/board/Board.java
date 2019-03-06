package net.marvk.chess.board;

public class Board {
    private static final int LENGTH = 8;
    private final ColoredPiece[][] board;
    private BoardState boardState;

    public Board() {
        this.board = new ColoredPiece[LENGTH][LENGTH];
    }

    public Board(final Fen fen) {
        this.board = new ColoredPiece[LENGTH][LENGTH];
        this.boardState = new BoardState(fen);

        Boards.parsePiecePlacement(fen.getPiecePlacement(), this.board);
    }

    public ColoredPiece getPiece(final Square square) {
        return getPiece(square.getRank(), square.getFile());
    }

    public ColoredPiece getPiece(final Rank rank, final File file) {
        return getPiece(rank.getIndex(), file.getIndex());
    }

    public ColoredPiece getPiece(final int rank, final int file) {
        return board[rank][file];
    }

    public static void main(final String[] args) {
        final Board board = new Board(Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));

        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                final ColoredPiece piece = board.getPiece(i, j);
                System.out.print(piece == null ? '.' : piece.getSan());
            }
            System.out.println();
        }
    }
}
