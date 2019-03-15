package net.marvk.chess.core.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.marvk.chess.core.board.ColoredPiece.*;

public class DefaultMoveStrategy {
    private static final List<Piece> PROMOTION_PIECES = Arrays.asList(Piece.QUEEN, Piece.ROOK, Piece.KNIGHT, Piece.BISHOP);
    private static final Square[] SQUARES = Square.values();
    private final Board board;
    private final Color color;
    private List<MoveResult> result;

    public DefaultMoveStrategy(final Board simpleBoard, final Color color) {
        this.board = simpleBoard;
        this.color = color;
    }

    public List<MoveResult> generate() {
        if (this.result != null) {
            return this.result;
        }

        this.result = new ArrayList<>();

        Arrays.stream(SQUARES)
              .map(sq -> new SquareColoredPiecePair(sq, board.getPiece(sq)))
              .filter(pair -> pair.getColoredPiece() != null)
              .filter(pair -> pair.getColoredPiece().getColor() == color)
              .forEach(pair -> applyStrategy(pair.getSquare(), board, pair.getColoredPiece()));

        return result;
    }

    public void applyStrategy(final Square square, final Board board, final ColoredPiece piece) {
        switch (piece) {
            case WHITE_KING:
                whiteKingStrategy(square, board);
                break;
            case WHITE_QUEEN:
                whiteQueenStrategy(square, board);
                break;
            case WHITE_ROOK:
                whiteRookStrategy(square, board);
                break;
            case WHITE_BISHOP:
                whiteBishopStrategy(square, board);
                break;
            case WHITE_KNIGHT:
                whiteKnightStrategy(square, board);
                break;
            case WHITE_PAWN:
                whitePawnStrategy(square, board);
                break;
            case BLACK_KING:
                blackKingStrategy(square, board);
                break;
            case BLACK_QUEEN:
                blackQueenStrategy(square, board);
                break;
            case BLACK_ROOK:
                blackRookStrategy(square, board);
                break;
            case BLACK_BISHOP:
                blackBishopStrategy(square, board);
                break;
            case BLACK_KNIGHT:
                blackKnightStrategy(square, board);
                break;
            case BLACK_PAWN:
                blackPawnStrategy(square, board);
                break;
            default:
                throw new AssertionError();
        }
    }

    private void blackKingStrategy(final Square square, final Board board) {
        generalKingStrategy(square, board, BLACK_KING);
    }

    private void blackQueenStrategy(final Square square, final Board board) {
        generalQueenStrategy(square, board, BLACK_QUEEN);
    }

    private void blackRookStrategy(final Square square, final Board board) {
        generalRookStrategy(square, board, BLACK_ROOK);
    }

    private void blackBishopStrategy(final Square square, final Board board) {
        generalBishopStrategy(square, board, BLACK_BISHOP);
    }

    private void blackKnightStrategy(final Square square, final Board board) {
        generalKnightStrategy(square, board, BLACK_KNIGHT);
    }

    private void blackPawnStrategy(final Square square, final Board board) {
        generalPawnStrategy(square, board, BLACK_PAWN);
    }

    private void whiteKingStrategy(final Square square, final Board board) {
        generalKingStrategy(square, board, WHITE_KING);
    }

    private void whiteQueenStrategy(final Square square, final Board board) {
        generalQueenStrategy(square, board, WHITE_QUEEN);
    }

    private void whiteRookStrategy(final Square square, final Board board) {
        generalRookStrategy(square, board, WHITE_ROOK);
    }

    private void whiteBishopStrategy(final Square square, final Board board) {
        generalBishopStrategy(square, board, WHITE_BISHOP);
    }

    private void whiteKnightStrategy(final Square square, final Board board) {
        generalKnightStrategy(square, board, WHITE_KNIGHT);
    }

    private void whitePawnStrategy(final Square square, final Board board) {
        generalPawnStrategy(square, board, WHITE_PAWN);
    }

    private void generalKingStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        generalSingleStepStrategy(square, board, Direction.CARDINAL_DIRECTIONS, coloredPiece);

        generateCastleMove(square, board, coloredPiece, Direction.WEST);

        generateCastleMove(square, board, coloredPiece, Direction.EAST);
    }

    private void generalQueenStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        generalMultiStepStrategy(square, board, Direction.CARDINAL_DIRECTIONS, coloredPiece);
    }

    private void generalRookStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        generalMultiStepStrategy(square, board, Direction.ORTHOGONAL_DIRECTIONS, coloredPiece);
    }

    private void generalBishopStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        generalMultiStepStrategy(square, board, Direction.DIAGONAL_DIRECTIONS, coloredPiece);
    }

    private void generalKnightStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        generalSingleStepStrategy(square, board, Direction.KNIGHT_DIRECTIONS, coloredPiece);
    }

    private void generalPawnStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        final boolean isWhite = coloredPiece.getColor() == Color.WHITE;
        final Direction forward = isWhite ? Direction.NORTH : Direction.SOUTH;
        final Rank startingRank = isWhite ? Rank.RANK_2 : Rank.RANK_7;
        final Direction westAttackDirection = isWhite ? Direction.NORTH_WEST : Direction.SOUTH_WEST;
        final Direction eastAttackDirection = isWhite ? Direction.NORTH_EAST : Direction.SOUTH_EAST;

        final Square enPassantTargetSquare = board.getState().getEnPassantTargetSquare();

        final Square next = square.translate(forward);

        if (isValidAndNotOccupied(next, board)) {
            generatePawnMoves(square, next, board, coloredPiece);

            if (startingRank == square.getRank()) {
                final Square afterNext = next.translate(forward);

                if (isValidAndNotOccupied(afterNext, board)) {
                    addMove(board.makeSimpleMove(Move.pawnDoubleMove(square, afterNext, coloredPiece)));
                }
            }
        }

        generatePawnAttack(square, square.translate(eastAttackDirection), Direction.EAST, board, enPassantTargetSquare, coloredPiece);
        generatePawnAttack(square, square.translate(westAttackDirection), Direction.WEST, board, enPassantTargetSquare, coloredPiece);
    }

    private void generatePawnAttack(final Square square, final Square attackSquare, final Direction west, final Board board, final Square enPassantTargetSquare, final ColoredPiece coloredPiece) {
        if (isValidAndOccupiedByAttackableOpponent(attackSquare, board, coloredPiece.getColor())) {
            generatePawnMoves(square, attackSquare, board, coloredPiece);
        }

        if (enPassantTargetSquare == attackSquare && isValidAndNotOccupied(enPassantTargetSquare, board)) {
            addMove(board.makeComplexMove(Move.enPassant(square, attackSquare, coloredPiece),
                    new SquareColoredPiecePair(square, null),
                    new SquareColoredPiecePair(attackSquare, coloredPiece),
                    new SquareColoredPiecePair(square.translate(west), null)
            ));
        }
    }

    private void generatePawnMoves(final Square source, final Square target, final Board board, final ColoredPiece coloredPiece) {
        final boolean blackPromotion = target.getRank() == Rank.RANK_1 && coloredPiece.getColor() == Color.BLACK;
        final boolean whitePromotion = target.getRank() == Rank.RANK_8 && coloredPiece.getColor() == Color.WHITE;

        if (blackPromotion || whitePromotion) {
            PROMOTION_PIECES.stream()
                            .map(p -> getPiece(coloredPiece.getColor(), p))
                            .map(promoteTo -> Move.promotion(source, target, coloredPiece, promoteTo))
                            .map(move -> board.makeComplexMove(move,
                                    new SquareColoredPiecePair(source, null),
                                    new SquareColoredPiecePair(target, move.getPromoteTo())
                            ))
                            .forEach(this::addMove);
        } else {
            addMove(board.makeSimpleMove(Move.simple(source, target, coloredPiece)));
        }
    }

    private static boolean isValidAndNotOccupied(final Square square, final Board board) {
        return square != null && board.getPiece(square) == null;
    }

    private static boolean isValidAndOccupiedByAttackableOpponent(final Square square, final Board board, final Color color) {
        final ColoredPiece piece = board.getPiece(square);
        return square != null && piece != null && piece.getPiece() != Piece.KING && piece.getColor() != color;
    }

    private void generateCastleMove(final Square source, final Board board, final ColoredPiece coloredPiece, final Direction direction) {
        final Color color = coloredPiece.getColor();

        // No castle while in check
        if (board.isInCheck(color, source)) {
            return;
        }

        if (color == Color.WHITE) {
            if (
                    direction == Direction.WEST
                            && board.getState().canWhiteCastleQueen()
                            && validCastle(source, Square.A1, Direction.WEST, board, color)
            ) {
                addMove(board.makeComplexMove(Move.castling(source, Square.C1, coloredPiece),
                        new SquareColoredPiecePair(source, null),
                        new SquareColoredPiecePair(Square.C1, coloredPiece),
                        new SquareColoredPiecePair(Square.D1, getPiece(color, Piece.ROOK)),
                        new SquareColoredPiecePair(Square.A1, null)
                ));
            } else if (
                    direction == Direction.EAST
                            && board.getState().canWhiteCastleKing()
                            && validCastle(source, Square.H1, Direction.EAST, board, color)
            ) {
                addMove(board.makeComplexMove(Move.castling(source, Square.G1, coloredPiece),
                        new SquareColoredPiecePair(source, null),
                        new SquareColoredPiecePair(Square.G1, coloredPiece),
                        new SquareColoredPiecePair(Square.F1, getPiece(color, Piece.ROOK)),
                        new SquareColoredPiecePair(Square.H1, null)
                ));
            }
        } else {
            if (
                    direction == Direction.WEST
                            && board.getState().canBlackCastleQueen()
                            && validCastle(source, Square.A8, Direction.WEST, board, color)
            ) {
                addMove(board.makeComplexMove(Move.castling(source, Square.C8, coloredPiece),
                        new SquareColoredPiecePair(source, null),
                        new SquareColoredPiecePair(Square.C8, coloredPiece),
                        new SquareColoredPiecePair(Square.D8, getPiece(color, Piece.ROOK)),
                        new SquareColoredPiecePair(Square.A8, null)
                ));
            } else if (
                    direction == Direction.EAST
                            && board.getState().canBlackCastleKing()
                            && validCastle(source, Square.H8, Direction.EAST, board, color)
            ) {
                addMove(board.makeComplexMove(Move.castling(source, Square.G8, coloredPiece),
                        new SquareColoredPiecePair(source, null),
                        new SquareColoredPiecePair(Square.G8, coloredPiece),
                        new SquareColoredPiecePair(Square.F8, getPiece(color, Piece.ROOK)),
                        new SquareColoredPiecePair(Square.H8, null)
                ));
            }
        }
    }

    private static boolean validCastle(final Square from, final Square to, final Direction direction, final Board board, final Color color) {
        Square current = from.translate(direction);

        while (current != to && current != null) {
            if (board.getPiece(current) != null || board.isInCheck(color, current)) {
                return false;
            }

            current = current.translate(direction);
        }

        return true;
    }

    private void generalMultiStepStrategy(final Square square, final Board board, final List<Direction> directions, final ColoredPiece coloredPiece) {
        for (final Direction direction : directions) {
            Square current = square;

            while (true) {
                current = current.translate(direction);

                if (isValidTarget(current, board, coloredPiece.getColor())) {
                    addMove(board.makeSimpleMove(Move.simple(square, current, coloredPiece)));

                    if (board.getPiece(current) != null) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void generalSingleStepStrategy(final Square square, final Board board, final List<Direction> directions, final ColoredPiece coloredPiece) {
        directions.stream()
                  .map(square::translate)
                  .filter(sq -> isValidTarget(sq, board, coloredPiece.getColor()))
                  .map(target -> Move.simple(square, target, coloredPiece))
                  .map(board::makeSimpleMove)
                  .forEach(this::addMove);
    }

    private static boolean isValidTarget(final Square target, final Board board, final Color sourceColor) {
        if (target == null) {
            return false;
        }

        final ColoredPiece piece = board.getPiece(target);

        if (piece == null) {
            return true;
        }

        return piece.getColor() != sourceColor && piece.getPiece() != Piece.KING;
    }

    private void addMove(final MoveResult moveResult) {
        if (moveResult.getBoard().isInCheck(color)) {
            return;
        }

        result.add(moveResult);
    }
}
