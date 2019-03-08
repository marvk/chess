package net.marvk.chess.board;

import lombok.Data;

@Data
public class GameResult {
    private final Color winner;
    private final EndCondition endCondition;
}
