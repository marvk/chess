package net.marvk.chess.board;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Board {
    ColoredPiece getPiece(Square square);

    ColoredPiece getPiece(File file, Rank rank);

    ColoredPiece getPiece(int file, int rank);

    ColoredPiece[][] getBoard();

    List<MoveResult> getValidMoves();

    List<MoveResult> getValidMovesForColor(Color color);

    MoveResult makeSimpleMove(final Move move);

    MoveResult makeComplexMove(final Move move, final SquareColoredPiecePair... pairs);

    BoardState getState();

    Optional<GameResult> findGameResult();

    double computeScore(final Map<Piece, Double> scoreMap, final Color color);

    boolean isInCheck();

    boolean isInCheck(Color color);

    boolean isInCheck(Color color, Square square);
}
