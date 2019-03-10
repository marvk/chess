package net.marvk.chess.board;

import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class SimpleBoard implements Board {
    private static final int LENGTH = 8;
    private static final Square[] SQUARES = Square.values();
    private final String board;
    private final BoardState boardState;

    public SimpleBoard(final Fen fen) {
        this.boardState = new BoardState(fen);

        this.board = Boards.parsePiecePlacement(fen.getPiecePlacement());
    }

    private SimpleBoard(final SimpleBoard simpleBoard, final BoardState boardState) {
        this.board = simpleBoard.board;
        this.boardState = boardState;
    }

    public SimpleBoard(final String board, final BoardState nextState) {
        this.board = board;
        this.boardState = nextState;
    }

    @Override
    public ColoredPiece getPiece(final int file, final int rank) {
        return ColoredPiece.getPieceFromSan(board.charAt(index(file, rank)));
    }

    @Override
    public ColoredPiece[][] getBoard() {
        throw new UnsupportedOperationException();
//        return IntStream.range(0, LENGTH)
//                        .mapToObj(i -> Arrays.copyOf(board[i], LENGTH))
//                        .toArray(ColoredPiece[][]::new);
    }

    private List<MoveResult> validMoves;

    @Override
    public List<MoveResult> getValidMoves() {
        if (validMoves == null) {
            //Modifiable!!
            validMoves = getValidMovesForColor(boardState.getActivePlayer());
        }

        return validMoves;
    }

    @Override
    public List<MoveResult> getValidMovesForColor(final Color color) {
        return new DefaultMoveStrategy(this, color).generate();
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

        final StringBuilder boardBuilder = new StringBuilder(board);

        for (final SquareColoredPiecePair swap : swaps) {
            final char san = swap.getColoredPiece() == null ? ' ' : swap.getColoredPiece().getSan();
            boardBuilder.setCharAt(index(swap.getSquare()), san);
        }

        return new MoveResult(new SimpleBoard(boardBuilder.toString(), nextState.build()), move);
    }

    @Override
    public BoardState getState() {
        return boardState;
    }

    @Override
    public Optional<GameResult> findGameResult() {
        EndCondition endCondition = null;
        Color winner = null;

        if (boardState.getHalfmoveClock() >= 50) {
            endCondition = EndCondition.DRAW_BY_FIFTY_MOVE_RULE;
        } else if (getValidMoves().isEmpty()) {
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

    private Double whiteScore;
    private Double blackScore;

    @Override
    public double computeScore(final Map<Piece, Double> scoreMap, final Color color) {
        Objects.requireNonNull(color);

        if (whiteScore == null) {
            whiteScore = 0.;
            blackScore = 0.;

            for (final Square square : SQUARES) {
                final ColoredPiece piece = getPiece(square);

                if (piece != null) {
                    final double score = scoreMap.get(piece.getPiece());

                    if (piece.getColor() == Color.WHITE) {
                        whiteScore += score;
                    } else {
                        blackScore += score;
                    }
                }
            }
        }

        if (color == Color.WHITE) {
            return whiteScore;
        }

        if (color == Color.BLACK) {
            return blackScore;
        }

        throw new AssertionError();
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

    private static int index(final Square square) {
        return square.getFile().getIndex() + square.getRank().getIndex() * 8;
    }

    private static int index(final int file, final int rank) {
        return file + rank * 8;
    }
}
