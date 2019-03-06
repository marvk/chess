package net.marvk.chess.board;

import java.util.List;

public interface MoveStrategy {
    List<MoveResult> blackKingStrategy(final Square square, final Board board);

    List<MoveResult> blackQueenStrategy(final Square square, final Board board);

    List<MoveResult> blackRookStrategy(final Square square, final Board board);

    List<MoveResult> blackBishopStrategy(final Square square, final Board board);

    List<MoveResult> blackKnightStrategy(final Square square, final Board board);

    List<MoveResult> blackPawnStrategy(final Square square, final Board board);

    List<MoveResult> whiteKingStrategy(final Square square, final Board board);

    List<MoveResult> whiteQueenStrategy(final Square square, final Board board);

    List<MoveResult> whiteRookStrategy(final Square square, final Board board);

    List<MoveResult> whiteBishopStrategy(final Square square, final Board board);

    List<MoveResult> whiteKnightStrategy(final Square square, final Board board);

    List<MoveResult> whitePawnStrategy(final Square square, final Board board);
}
