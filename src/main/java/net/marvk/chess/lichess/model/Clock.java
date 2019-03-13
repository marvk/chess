package net.marvk.chess.lichess.model;

import lombok.Data;

@Data
public class Clock {
    private final Integer initial;
    private final Integer increment;
}
