package net.marvk.chess.lichess4j.model;

import lombok.Data;

@Data
public class TimeControl {
    private final String type;
    private final Integer limit;
    private final Integer increment;
    private final String show;
}
