package net.marvk.chess.lichess4j.model;

import com.google.gson.annotations.SerializedName;

public enum Room {
    @SerializedName("player")
    PLAYER("player"),
    @SerializedName("spectator")
    SPECTATOR("spectator");

    private final String representation;

    Room(final String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
