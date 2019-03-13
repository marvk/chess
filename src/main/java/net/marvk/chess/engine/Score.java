package net.marvk.chess.engine;

import lombok.Data;

@Data
public class Score {
    private final Double scoreInCentipawns;
    private final Integer mateIn;
    private final Boolean scoreIsLowerBound;
    private final Boolean scoreIsUpperBound;

    public String toCommand() {
        //TODO
        throw new UnsupportedOperationException();
    }
}
