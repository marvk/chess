package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MagicBitboard implements Board {
    @Override
    public ColoredPiece getPiece(final int file, final int rank) {
        return null;
    }

    @Override
    public ColoredPiece[][] getBoard() {
        return new ColoredPiece[0][];
    }

    @Override
    public List<MoveResult> getValidMovesForColor(final Color color) {
        return null;
    }

    @Override
    public MoveResult makeSimpleMove(final Move move) {
        return null;
    }

    @Override
    public MoveResult makeComplexMove(final Move move, final SquareColoredPiecePair... pairs) {
        return null;
    }

    @Override
    public BoardState getState() {
        return null;
    }

    @Override
    public Optional<GameResult> findGameResult() {
        return Optional.empty();
    }

    @Override
    public double computeScore(final Map<Piece, Double> scoreMap, final Color color) {
        return 0;
    }

    @Override
    public boolean isInCheck(final Color color) {
        return false;
    }

    @Override
    public boolean isInCheck(final Color color, final Square square) {
        return false;
    }
}
