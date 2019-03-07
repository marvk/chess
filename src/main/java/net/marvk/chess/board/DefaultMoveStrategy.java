package net.marvk.chess.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultMoveStrategy implements MoveStrategy {
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

    @Override
    public List<MoveResult> blackKingStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> blackQueenStrategy(final Square square, final Board board) {
        return generalQueenStrategy(square, board, ColoredPiece.BLACK_QUEEN);
    }

    @Override
    public List<MoveResult> blackRookStrategy(final Square square, final Board board) {
        return generalRookStrategy(square, board, ColoredPiece.BLACK_ROOK);
    }

    @Override
    public List<MoveResult> blackBishopStrategy(final Square square, final Board board) {
        return generalBishopStrategy(square, board, ColoredPiece.BLACK_BISHOP);
    }

    @Override
    public List<MoveResult> blackKnightStrategy(final Square square, final Board board) {
        return generalKnightStrategy(square, board, ColoredPiece.BLACK_KNIGHT);
    }

    @Override
    public List<MoveResult> blackPawnStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> whiteKingStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> whiteQueenStrategy(final Square square, final Board board) {
        return generalQueenStrategy(square, board, ColoredPiece.WHITE_QUEEN);
    }

    @Override
    public List<MoveResult> whiteRookStrategy(final Square square, final Board board) {
        return generalRookStrategy(square, board, ColoredPiece.WHITE_ROOK);
    }

    @Override
    public List<MoveResult> whiteBishopStrategy(final Square square, final Board board) {
        return generalBishopStrategy(square, board, ColoredPiece.WHITE_BISHOP);
    }

    @Override
    public List<MoveResult> whiteKnightStrategy(final Square square, final Board board) {
        return generalKnightStrategy(square, board, ColoredPiece.WHITE_KNIGHT);
    }

    @Override
    public List<MoveResult> whitePawnStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    private static List<MoveResult> generalQueenStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        return generalCardinalDirectionStrategy(square, board, Direction.CARDINAL_DIRECTIONS, coloredPiece);
    }

    private static List<MoveResult> generalRookStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        return generalCardinalDirectionStrategy(square, board, Direction.ORTHOGONAL_DIRECTIONS, coloredPiece);
    }

    private static List<MoveResult> generalBishopStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        return generalCardinalDirectionStrategy(square, board, Direction.DIAGONAL_DIRECTIONS, coloredPiece);
    }

    private static List<MoveResult> generalCardinalDirectionStrategy(final Square square, final Board board, final List<Direction> directions, final ColoredPiece coloredPiece) {
        final List<MoveResult> result = new ArrayList<>();

        for (final Direction direction : directions) {
            Square current = square;

            while (true) {
                current = current.translate(direction);

                if (isValidTarget(current, board, coloredPiece.getColor())) {
                    result.add(board.makeMove(new Move(square, current, coloredPiece)));

                    if (board.getPiece(current) != null) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        result.stream().map(MoveResult::getMove).forEach(System.out::println);

        return result;
    }

    private static List<MoveResult> generalKnightStrategy(final Square square, final Board board, final ColoredPiece coloredPiece) {
        return Direction.KNIGHT_DIRECTIONS
                .stream()
                .map(square::translate)
                .filter(sq -> isValidTarget(sq, board, coloredPiece.getColor()))
                .map(target -> new Move(square, target, coloredPiece))
                .map(board::makeMove)
                .collect(Collectors.toList());
    }
}
