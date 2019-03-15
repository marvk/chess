package net.marvk.chess.ui.application.view.board;

import lombok.Data;
import net.marvk.chess.core.board.ColoredPiece;
import net.marvk.chess.core.board.Move;
import net.marvk.chess.core.board.Square;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
class CellViewModel {
    public static final CellViewModel EMPTY = new CellViewModel(null, null, Collections.emptyMap(), Collections.emptyMap(), Move.NULL_MOVE);

    private final ColoredPiece coloredPiece;
    private final Square square;
    private final Map<Move, Double> values;
    private final Map<Square, List<Move>> validMoves;
    private final Move lastMove;
}
