package net.marvk.chess.uci4j;

@FunctionalInterface
public interface EngineFactory {
    SimpleUciEngine create(final UiChannel uiChannel);
}
