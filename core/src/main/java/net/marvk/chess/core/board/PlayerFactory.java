package net.marvk.chess.core.board;

@FunctionalInterface
public interface PlayerFactory {
    Player create(Color color);
}
