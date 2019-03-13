package net.marvk.chess.lichess.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import net.marvk.chess.engine.UciMove;

@Data
public class GameState {
    private final UciMove[] moves;
    @SerializedName("wtime")
    private final Integer whiteTime;
    @SerializedName("btime")
    private final Integer blackTime;
    @SerializedName("winc")
    private final Integer whiteIncrement;
    @SerializedName("binc")
    private final Integer blackIncrement;
}
