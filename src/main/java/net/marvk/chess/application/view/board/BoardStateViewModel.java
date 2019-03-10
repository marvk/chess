package net.marvk.chess.application.view.board;

import lombok.Data;
import net.marvk.chess.board.Board;
import net.marvk.chess.board.Move;

import java.util.Map;

@Data
public class BoardStateViewModel {
    private final Board newBoard;
    private final Move lastMove;
    private final Map<Move, Double> lastEvaluation;
}
