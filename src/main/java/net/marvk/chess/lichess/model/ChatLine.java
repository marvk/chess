package net.marvk.chess.lichess.model;

import lombok.Data;

@Data
public class ChatLine {
    private final String username;
    private final String text;
    private final Room room;
}
