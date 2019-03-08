package net.marvk.chess.board;

@FunctionalInterface
public interface PlayerFactory {
    Player create(Color color);
}
