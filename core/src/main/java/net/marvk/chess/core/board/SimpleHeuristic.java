package net.marvk.chess.core.board;

import net.marvk.chess.core.bitboards.Bitboard;

import java.util.Optional;

public class SimpleHeuristic implements Heuristic {

    @Override
    public int evaluate(final Bitboard board, final Color self) {
        final int mySum = board.computeScore(self);
        final int theirSum = board.computeScore(self.opposite());

        final Optional<GameResult> gameResult = board.findGameResult();

        if (gameResult.isPresent()) {
            final Color winner = gameResult.get().getWinner();

            if (winner == null) {
                return 0;
            }

            final int timePenalty = board.getFullmoveClock();

            return winner == self ? Integer.MAX_VALUE - timePenalty : Integer.MIN_VALUE;
        }

        final int pieceSquareValue = board.pieceSquareValue(self);

        final int result = mySum - theirSum + pieceSquareValue;

        return result;
    }
}
