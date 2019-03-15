package net.marvk.chess.lichess4j.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameStateFull {
    @SerializedName("id")
    private final String gameId;
    private final Boolean rated;
    private final Variant variant;
    private final Clock clock;
    private final String speed;
    private final Perf perf;
    private final LocalDateTime createdAt;
    private final UserData white;
    private final UserData black;
    private final String initialFen;
    @SerializedName("state")
    private final GameState gameState;
}
