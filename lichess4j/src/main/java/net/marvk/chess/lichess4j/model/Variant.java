package net.marvk.chess.lichess4j.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Variant {
    private final String key;
    private final Name name;
    @SerializedName("short")
    private final String shortName;

    public enum Name {

    }
}
