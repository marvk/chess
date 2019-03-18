package net.marvk.chess.ui.model;

import net.marvk.chess.core.board.Color;

@FunctionalInterface
public interface PlayerFactory {
    Player create(Color color);
}
