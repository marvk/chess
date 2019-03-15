package net.marvk.chess.core.board;

import lombok.Data;

@Data
public class GameResult {
    private final Color winner;
    private final EndCondition endCondition;
}
