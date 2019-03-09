package net.marvk.chess.application.view.board;

import lombok.Data;
import net.marvk.chess.board.ColoredPiece;
import net.marvk.chess.board.Move;
import net.marvk.chess.board.Square;

import java.util.List;
import java.util.Map;

@Data
class CellViewModel {
    private final ColoredPiece coloredPiece;
    private final Square square;
    private final Map<Move, Double> values;
    private final Map<Square, List<Move>> validMoves;
    private final Move lastMove;
}
