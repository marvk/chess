package net.marvk.chess.uci4j;

@FunctionalInterface
public interface EngineFactory {
    Engine create(final UIChannel uiChannel);
}
