package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleBoard implements Board {
    private static final MoveStrategy MOVE_STRATEGY = new DefaultMoveStrategy();

    private static final int LENGTH = 8;
    private final ColoredPiece[][] board;
    private final BoardState boardState;

    public SimpleBoard(final Fen fen) {
        this.board = new ColoredPiece[LENGTH][LENGTH];
        this.boardState = new BoardState(fen);

        Boards.parsePiecePlacement(fen.getPiecePlacement(), this.board);
    }

    public SimpleBoard(final SimpleBoard simpleBoard) {
        this.board = new ColoredPiece[LENGTH][LENGTH];

        for (int i = 0; i < this.board.length; i++) {
            System.arraycopy(simpleBoard.board[i], 0, this.board[i], 0, LENGTH);
        }

        //TODO
        this.boardState = null;
    }

    @Override
    public ColoredPiece getPiece(final Square square) {
        System.out.println("square = " + square);
        return getPiece(square.getRank(), square.getFile());
    }

    @Override
    public ColoredPiece getPiece(final Rank rank, final File file) {
        System.out.println("rank = " + rank);
        System.out.println("file = " + file);
        return getPiece(rank.getIndex(), file.getIndex());
    }

    @Override
    public ColoredPiece getPiece(final int rank, final int file) {
        System.out.println("rank = " + rank);
        System.out.println("file = " + file);
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
        return Arrays.stream(Square.values())
                     .filter(square -> getPiece(square) != null)
                     .map(square -> getPiece(square).applyStrategy(MOVE_STRATEGY, square, this))
                     .flatMap(Collection::stream)
                     .collect(Collectors.toList());
    }

    @Override
    public MoveResult makeMove(final Move move) {
        final SimpleBoard result = new SimpleBoard(this);

        result.setPiece(move.getSource(), null);
        result.setPiece(move.getTarget(), move.getColoredPiece());

        return new MoveResult(result, move);
    }

    private void setPiece(final Square source, final ColoredPiece piece) {
        board[source.getRank().getIndex()][source.getFile().getIndex()] = piece;
    }

    public static void main(final String[] args) {
        final Board board = new SimpleBoard(Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));

//        final List<MoveResult> validMoves = board.getValidMoves(Color.BLACK);

        System.out.println(board.getPiece(Square.D1));
        System.out.println();
        System.out.println(board.getPiece(0, 3));

//        System.out.println(validMoves);

//        for (int i = 0; i < LENGTH; i++) {
//            for (int j = 0; j < LENGTH; j++) {
//                final ColoredPiece piece = board.getPiece(i, j);
//                System.out.print(piece == null ? '.' : piece.getSan());
//            }
//            System.out.println();
//        }
    }
}
