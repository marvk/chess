package net.marvk.chess.board;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class SimpleBoard implements Board {
    private static final int LENGTH = 8;
    private final ColoredPiece[][] board;
    private BoardState boardState;

    public SimpleBoard() {
        this.board = new ColoredPiece[LENGTH][LENGTH];
    }

    public SimpleBoard(final Fen fen) {
        this.board = new ColoredPiece[LENGTH][LENGTH];
        this.boardState = new BoardState(fen);

        Boards.parsePiecePlacement(fen.getPiecePlacement(), this.board);
    }

    @Override
    public ColoredPiece getPiece(final Square square) {
        return getPiece(square.getRank(), square.getFile());
    }

    @Override
    public ColoredPiece getPiece(final Rank rank, final File file) {
        return getPiece(rank.getIndex(), file.getIndex());
    }

    @Override
    public ColoredPiece getPiece(final int rank, final int file) {
        return board[rank][file];
    }

    @Override
    public ColoredPiece[][] getBoard() {
        return IntStream.range(0, LENGTH)
                        .mapToObj(i -> Arrays.copyOf(board[i], LENGTH))
                        .toArray(ColoredPiece[][]::new);
    }

    @Override
    public List<MoveResult> getValidMoves(final Color color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MoveResult makeMove(final ColoredPiece coloredPiece, final Square square) {
        return null;
    }

    public static void main(final String[] args) {
        final Board board = new SimpleBoard(Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));

        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                final ColoredPiece piece = board.getPiece(i, j);
                System.out.print(piece == null ? '.' : piece.getSan());
            }
            System.out.println();
        }
    }
}
