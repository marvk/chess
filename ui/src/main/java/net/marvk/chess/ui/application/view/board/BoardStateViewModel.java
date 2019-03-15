package net.marvk.chess.ui.application.view.board;

import lombok.Data;
import net.marvk.chess.core.board.Board;
import net.marvk.chess.core.board.Move;

import java.util.Map;

@Data
public class BoardStateViewModel {
    private final Board newBoard;
    private final Move lastMove;
    private final Map<Move, Double> lastEvaluation;
    private final Double evaluation;
}
