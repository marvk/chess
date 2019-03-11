package net.marvk.chess.lichess;

public enum Room {
    PLAYER("player"),
    SPECTATOR("spectator");

    private final String representation;

    Room(final String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
