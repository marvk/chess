package net.marvk.chess.board;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
public class SimpleBoard implements Board {
    private static final MoveStrategy MOVE_STRATEGY = new DefaultMoveStrategy();

    private static final int LENGTH = 8;
    public static final Square[] SQUARES = Square.values();
    private final ColoredPiece[][] board;
    private final BoardState boardState;

    public SimpleBoard(final Fen fen) {
        this.board = new ColoredPiece[LENGTH][LENGTH];
        this.boardState = new BoardState(fen);

        Boards.parsePiecePlacement(fen.getPiecePlacement(), this.board);
    }

    private SimpleBoard(final SimpleBoard simpleBoard, final BoardState boardState) {
        this.board = new ColoredPiece[LENGTH][LENGTH];

        for (int i = 0; i < this.board.length; i++) {
            System.arraycopy(simpleBoard.board[i], 0, this.board[i], 0, LENGTH);
        }

        this.boardState = boardState;
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

    private List<MoveResult> validMoves;

    @Override
    public List<MoveResult> getValidMoves() {
        if (validMoves == null) {
            final Color activePlayer = boardState.getActivePlayer();
            validMoves = Collections.unmodifiableList(getValidMovesForColor(activePlayer));
        }

        return validMoves;
    }

    @Override
    public List<MoveResult> getValidMovesForColor(final Color color) {
        return Arrays.stream(SQUARES)
                     .map(sq -> new SquareColoredPiecePair(sq, getPiece(sq)
                     ))
                     .filter(pair -> pair.getColoredPiece() != null)
                     .filter(pair -> pair.getColoredPiece().getColor() == color)
                     .map(pair -> pair.getColoredPiece()
                                      .applyStrategy(MOVE_STRATEGY, pair.getSquare(), this))
                     .flatMap(Collection::stream)
                     .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @Override
    public MoveResult makeSimpleMove(final Move move) {
        return makeComplexMove(
                move,
                new SquareColoredPiecePair(move.getSource(), null),
                new SquareColoredPiecePair(move.getTarget(), move.getColoredPiece())
        );
    }

    @Override
    public MoveResult makeComplexMove(final Move move, final SquareColoredPiecePair... swaps) {
        final BoardState.BoardStateBuilder nextState = boardState.nextBuilder();

        final boolean pawnMoved = move.getColoredPiece().getPiece() == Piece.PAWN;
        final boolean pieceAttacked = getPiece(move.getTarget()) != null;

        if (pawnMoved || pieceAttacked) {
            nextState.halfmoveReset();
        }

        if (move.isPawnDoubleMove()) {
            final Direction direction = move.getColoredPiece()
                                            .getColor() == Color.WHITE ? Direction.SOUTH : Direction.NORTH;

            final Square enPassantSquare = move.getTarget().translate(direction);

            nextState.possibleEnPassant(enPassantSquare);
        }

        if (move.getColoredPiece() == ColoredPiece.WHITE_KING) {
            nextState.lostCastle(Color.WHITE);
        } else if (move.getColoredPiece() == ColoredPiece.BLACK_KING) {
            nextState.lostCastle(Color.BLACK);
        }

        if (move.getColoredPiece() == ColoredPiece.WHITE_ROOK) {
            if (move.getSource() == Square.A1) {
                nextState.lostQueenSideCastle(Color.WHITE);
            } else if (move.getSource() == Square.H1) {
                nextState.lostKingSideCastle(Color.WHITE);
            }
        } else if (move.getColoredPiece() == ColoredPiece.BLACK_ROOK) {
            if (move.getSource() == Square.A8) {
                nextState.lostQueenSideCastle(Color.BLACK);
            } else if (move.getSource() == Square.H8) {
                nextState.lostKingSideCastle(Color.BLACK);
            }
        }

        final SimpleBoard result = new SimpleBoard(this, nextState.build());

        for (final SquareColoredPiecePair swap : swaps) {
            result.setPiece(swap.getSquare(), swap.getColoredPiece());
        }

        return new MoveResult(result, move);
    }

    @Override
    public BoardState getState() {
        return boardState;
    }

    @Override
    public Optional<GameResult> findGameResult() {
        EndCondition endCondition = null;

        if (boardState.getHalfmoveClock() >= 50) {
            endCondition = EndCondition.DRAW_BY_FIFTY_MOVE_RULE;
        }

        Color winner = null;

        if (getValidMoves().isEmpty()) {
            if (isInCheck()) {
                endCondition = EndCondition.CHECKMATE;
                winner = boardState.getActivePlayer().opposite();
            } else {
                endCondition = EndCondition.DRAW_BY_STALEMATE;
            }
        }

        if (endCondition == null) {
            return Optional.empty();
        }

        return Optional.of(new GameResult(winner, endCondition));
    }

    @Override
    public boolean isInCheck() {
        return isInCheck(boardState.getActivePlayer());
    }

    @Override
    public boolean isInCheck(final Color color) {
        final Optional<Square> maybeKingSquare =
                Arrays.stream(SQUARES)
                      .filter(square -> getPiece(square) == ColoredPiece.getPiece(color, Piece.KING))
                      .findFirst();

        if (!maybeKingSquare.isPresent()) {
            return false;
        }

        final Square kingSquare = maybeKingSquare.get();

        return isInCheck(color, kingSquare);
    }

    @Override
    public boolean isInCheck(final Color color, final Square square) {
        for (final Direction direction : Direction.KNIGHT_DIRECTIONS) {
            if (discoverPieceSingleStep(square, direction) == ColoredPiece.getPiece(color.opposite(), Piece.KNIGHT)) {
                return true;
            }
        }

        for (final Direction direction : Direction.CARDINAL_DIRECTIONS) {
            if (discoverPieceSingleStep(square, direction) == ColoredPiece.getPiece(color.opposite(), Piece.KING)) {
                return true;
            }
        }

        for (final Direction direction : Direction.ORTHOGONAL_DIRECTIONS) {
            final ColoredPiece coloredPiece = discoverPieceMultiStep(square, direction);
            if (coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.QUEEN)
                    || coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.ROOK)) {
                return true;
            }
        }

        for (final Direction direction : Direction.DIAGONAL_DIRECTIONS) {
            final ColoredPiece coloredPiece = discoverPieceMultiStep(square, direction);

            if (coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.QUEEN)
                    || coloredPiece == ColoredPiece.getPiece(color.opposite(), Piece.BISHOP)) {
                return true;
            }
        }

        final Direction westAttackDirection = color == Color.WHITE ? Direction.NORTH_WEST : Direction.SOUTH_WEST;
        final Direction eastAttackDirection = color == Color.WHITE ? Direction.NORTH_EAST : Direction.SOUTH_EAST;

        final ColoredPiece oppositePawn = ColoredPiece.getPiece(color.opposite(), Piece.PAWN);

        if (getPiece(square.translate(westAttackDirection)) == oppositePawn) {
            return true;
        }

        if (getPiece(square.translate(eastAttackDirection)) == oppositePawn) {
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
