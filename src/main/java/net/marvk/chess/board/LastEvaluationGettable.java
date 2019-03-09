package net.marvk.chess.board;

import java.util.Map;

public interface LastEvaluationGettable {
    Map<Move, Double> getLastEvaluation();
}
