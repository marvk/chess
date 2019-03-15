package net.marvk.chess.core.board;

import java.util.Map;

public interface LastEvaluationGettable {
    Map<Move, Double> getLastEvaluation();
}
