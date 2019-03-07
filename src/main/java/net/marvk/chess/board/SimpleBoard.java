package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
        this.boardState = simpleBoard.boardState;
    }

    @Override
    public ColoredPiece getPiece(final Square square) {
        if (square == null) {
            return null;
        }

        return getPiece(square.getFile(), square.getRank());
    }

    @Override
    public ColoredPiece getPiece(final File file, final Rank rank) {
        return getPiece(file.getIndex(), rank.getIndex());
    }

    @Override
    public ColoredPiece getPiece(final int file, final int rank) {
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
    public MoveResult makeSimpleMove(final Move move) {
        final SimpleBoard result = new SimpleBoard(this);

        result.setPiece(move.getSource(), null);
        result.setPiece(move.getTarget(), move.getColoredPiece());

        return new MoveResult(result, move);
    }

    @Override
    public MoveResult makeComplexMove(final Move move, final Collection<SquareColoredPiecePair> swaps) {
        final SimpleBoard result = new SimpleBoard(this);

        for (final SquareColoredPiecePair swap : swaps) {
            result.setPiece(swap.getSquare(), swap.getColoredPiece());
        }

        return new MoveResult(result, null);
    }

    @Override
    public BoardState getState() {
        return boardState;
    }

    @Override
    public boolean isInCheck(final Color color) {
        final Optional<Square> maybeKingSquare =
                Arrays.stream(Square.values())
                      .filter(square -> getPiece(square) == ColoredPiece.getPiece(color, Piece.KING))
                      .findFirst();

        if (!maybeKingSquare.isPresent()) {
            return false;
        }

        final Square kingSquare = maybeKingSquare.get();

        for (final Direction direction : Direction.KNIGHT_DIRECTIONS) {
            if (discoverPieceSingleStep(kingSquare, direction) == ColoredPiece.getPiece(color.opposite(), Piece.KNIGHT)) {
                return true;
            }
        }

        for (final Direction direction : Direction.CARDINAL_DIRECTIONS) {
            if (discoverPieceSingleStep(kingSquare, direction) == ColoredPiece.getPiece(color.opposite(), Piece.KING)) {
                return true;
            }
        }

        for (final Direction direction : Direction.ORTHOGONAL_DIRECTIONS) {
            final ColoredPiece coloredPiece = discoverPieceMultiStep(kingSquare, direction);
            if (coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.QUEEN)
                    || coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.ROOK)) {
                return true;
            }
        }

        for (final Direction direction : Direction.DIAGONAL_DIRECTIONS) {
            final ColoredPiece coloredPiece = discoverPieceMultiStep(kingSquare, direction);

            System.out.println(coloredPiece);

            if (coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.QUEEN)
                    || coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.BISHOP)) {
                return true;
            }
        }

        final Direction westAttackDirection = color == Color.WHITE ? Direction.NORTH_WEST : Direction.SOUTH_WEST;
        final Direction eastAttackDirection = color == Color.WHITE ? Direction.NORTH_EAST : Direction.SOUTH_EAST;

        final ColoredPiece oppositePawn = ColoredPiece.getPiece(color.opposite(), Piece.PAWN);

        if (getPiece(kingSquare.translate(westAttackDirection)) == oppositePawn) {
            return true;
        }

        if (getPiece(kingSquare.translate(eastAttackDirection)) == oppositePawn) {
            return true;
        }

        return false;
    }

    private ColoredPiece discoverPieceMultiStep(final Square source, final Direction direction) {
        Square current = source;

        while (current != null) {
            current = current.translate(direction);

            final ColoredPiece piece = getPiece(current);

            if (piece != null) {
                return piece;
            }
        }

        return null;
    }

    private ColoredPiece discoverPieceSingleStep(final Square source, final Direction direction) {
        return getPiece(source.translate(direction));
    }

    private void setPiece(final Square source, final ColoredPiece piece) {
        board[source.getRank().getIndex()][source.getFile().getIndex()] = piece;
    }
}
