package net.marvk.chess.core.board;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MagicBitBoard implements Board {
    @Override
    public ColoredPiece getPiece(final int file, final int rank) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColoredPiece[][] getBoard() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MoveResult> getValidMovesForColor(final Color color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MoveResult makeSimpleMove(final Move move) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MoveResult makeComplexMove(final Move move, final SquareColoredPiecePair... pairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BoardState getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<GameResult> findGameResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double computeScore(final Map<Piece, Double> scoreMap, final Color color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInCheck(final Color color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInCheck(final Color color, final Square square) {
        throw new UnsupportedOperationException();
    }
}
