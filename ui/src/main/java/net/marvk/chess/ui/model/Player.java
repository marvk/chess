package net.marvk.chess.ui.model;

import net.marvk.chess.core.board.Color;
import net.marvk.chess.core.board.Move;
import net.marvk.chess.core.board.MoveResult;

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
