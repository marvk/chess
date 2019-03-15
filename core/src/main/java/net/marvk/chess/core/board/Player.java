package net.marvk.chess.core.board;

public abstract class Player {
    private final Color color;

    public Player(final Color color) {
        this.color = color;
    }

    public abstract Move play(final MoveResult previousMove);

    public Color getColor() {
        return color;
    }
}
