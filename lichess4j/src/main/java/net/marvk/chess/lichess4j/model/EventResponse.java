package net.marvk.chess.lichess4j.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <a href="https://lichess.org/api#tag/Board/operation/apiStreamEvent">Lichess Event Stream Documentation</a>
 */
@Data
public class EventResponse {
    private final Type type;
    private final Challenge challenge;
    private final Game game;

    /**
     * <a href="https://lichess.org/api#tag/Board/operation/apiStreamEvent">Lichess Event Stream Documentation</a>
     */
    public enum Type {
        @SerializedName("challenge") CHALLENGE,
        @SerializedName("challengeCanceled") CHALLENGE_CANCELED,
        @SerializedName("challengeDeclined") CHALLENGE_DECLINED,
        @SerializedName("gameFinish") GAME_FINISH,
        @SerializedName("gameStart") GAME_START;

        /**
         * Returns all values of the enum as a list.<br>
         * Alternative to {@link Type#values()}.
         */
        public static List<Type> toList() {
            return Arrays.stream(Type.values()).collect(Collectors.toList());
        }
    }
}
