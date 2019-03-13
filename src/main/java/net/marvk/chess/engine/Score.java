package net.marvk.chess.engine;

import lombok.Data;

@Data
public class Score {
    private final Integer scoreInCentipawns;
    private final Integer mateIn;
    private final Bound bound;

    public String toCommand() {
        final String boundCommand;

        if (bound != null) {
            boundCommand = " " + bound.command;
        } else {
            boundCommand = "";
        }

        return "score"
                + CommandUtil.toCommand("cp", scoreInCentipawns)
                + CommandUtil.toCommand("mate", mateIn)
                + boundCommand;
    }

    public enum Bound {
        LOWER("lowerbound"),
        UPPER("upperbound");

        private final String command;

        Bound(final String command) {
            this.command = command;
        }
    }
}
