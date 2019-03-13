package net.marvk.chess.lichess.model;

import com.google.gson.annotations.SerializedName;

public enum Room {
    @SerializedName("player")
    PLAYER,
    @SerializedName("spectator")
    SPECTATOR;
}
