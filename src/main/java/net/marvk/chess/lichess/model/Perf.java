package net.marvk.chess.lichess.model;

import com.google.gson.annotations.SerializedName;

public enum Perf {
    @SerializedName(value = "ultraBullet", alternate = {"ultrabullet"})
    ULTRA_BULLET,
    @SerializedName(value = "bullet")
    BULLET,
    @SerializedName(value = "blitz")
    BLITZ,
    @SerializedName(value = "rapid")
    RAPID,
    @SerializedName(value = "classical")
    CLASSICAL,
    @SerializedName(value = "correspondence")
    CORRESPONDENCE,
    @SerializedName(value = "chess960")
    CHESS960,
    @SerializedName(value = "crazyhouse")
    CRAZYHOUSE,
    @SerializedName(value = "antichess")
    ANTICHESS,
    @SerializedName(value = "atomic")
    ATOMIC,
    @SerializedName(value = "horde")
    HORDE,
    @SerializedName(value = "kingOfTheHill", alternate = "kingofthehill")
    KING_OF_THE_HILL,
    @SerializedName(value = "racingKings", alternate = "racingkings")
    RACING_KINGS,
    @SerializedName(value = "threeCheck", alternate = "threecheck")
    THREE_CHECK;
}
