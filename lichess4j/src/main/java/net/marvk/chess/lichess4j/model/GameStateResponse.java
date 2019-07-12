package net.marvk.chess.lichess4j.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NonNull;

@Data
public class GameStateResponse {
    private final @NonNull Type type;
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
