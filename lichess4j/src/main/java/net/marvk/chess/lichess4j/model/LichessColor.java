package net.marvk.chess.lichess4j.model;

import com.google.gson.annotations.SerializedName;

public enum LichessColor {
    @SerializedName("white")
    WHITE,
    @SerializedName("black")
    BLACK,
    @SerializedName("random")
    RANDOM;
}
