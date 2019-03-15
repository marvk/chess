package net.marvk.chess.lichess4j.model;

import lombok.Data;

@Data
public class ChatLine {
    private final String username;
    private final String text;
    private final Room room;
}
