package net.marvk.chess.board;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultMoveStrategy implements MoveStrategy {
    @Override
    public List<MoveResult> blackKingStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> blackQueenStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> blackRookStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> blackBishopStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> blackKnightStrategy(final Square square, final Board board) {
        final List<Square> collect = Direction.KNIGHT_DIRECTIONS.stream()
                                                                .map(square::translate)
                                                                .filter(Objects::nonNull)
                                                                .collect(Collectors.toList());

        final Iterator<Square> iterator = collect.iterator();

        while (iterator.hasNext()) {
            final Square next = iterator.next();

            final ColoredPiece piece = board.getPiece(next);

            if (piece == null) {
                continue;
            }

            if (piece.getColor() == Color.BLACK || piece.getPiece() == Piece.KING) {
                iterator.remove();
            }
        }

        return collect.stream()
                      .map(target -> new Move(square, target, ColoredPiece.BLACK_KNIGHT))
                      .map(board::makeMove)
                      .collect(Collectors.toList());
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
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> whiteRookStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> whiteBishopStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> whiteKnightStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<MoveResult> whitePawnStrategy(final Square square, final Board board) {
        return Collections.emptyList();
    }
}
