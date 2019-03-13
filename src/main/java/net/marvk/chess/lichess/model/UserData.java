package net.marvk.chess.lichess.model;

import lombok.Data;

@Data
public class UserData {
    private final String id;
    private final String name;
    private final String title;
    private final Integer rating;
    private final Boolean provisional;
    private final Boolean patron;
    private final Boolean online;
    private final Integer lag;
}
