package net.marvk.chess.lichess.model;

import lombok.Data;

@Data
public class Challenge {
    private final String id;
    private final String status;
    private final UserData challenger;
    private final UserData destUser;
    private final Variant variant;
    private final Boolean rated;
    private final TimeControl timeControl;
    private final LichessColor color;
    private final Perf perf;
}
