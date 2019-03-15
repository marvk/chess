package net.marvk.chess.lichess4j.model;

import lombok.Data;

@Data
public class Clock {
    private final Integer initial;
    private final Integer increment;
}
