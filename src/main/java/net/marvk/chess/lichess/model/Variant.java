package net.marvk.chess.lichess.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Variant {
    private final String key;
    private final String name;
    @SerializedName("short")
    private final String shortName;
}
