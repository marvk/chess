package net.marvk.chess.lichess4j.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class EventResponse {
    private final Type type;
    private final Challenge challenge;
    @SerializedName("game")
    private final GameStart gameStart;

    public enum Type {
        @SerializedName("challenge")
        CHALLENGE,
        @SerializedName("gameStart")
        GAME_START;
    }
}
