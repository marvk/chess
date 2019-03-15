package net.marvk.chess.uci4j;

@FunctionalInterface
public interface EngineFactory {
    UciEngine create(final UIChannel uiChannel);
}
