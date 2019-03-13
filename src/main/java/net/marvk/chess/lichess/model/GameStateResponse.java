package net.marvk.chess.lichess.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class GameStateResponse {
    private final Type type;
    private final GameStateFull gameStateFull;
    private final GameState gameState;
    private final ChatLine chatLine;

    public enum Type {
        @SerializedName("gameFull")
        GAME_FULL,
        @SerializedName("gameState")
        GAME_STATE,
        @SerializedName("chatLine")
        CHAT_LINE;
    }
}
