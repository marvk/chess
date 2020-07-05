package net.marvk.chess.uci4j;

public abstract class SimpleUciEngine implements UciEngine {
    protected final UiChannel uiChannel;

    public SimpleUciEngine(final UiChannel uiChannel) {
        this.uiChannel = uiChannel;
    }
}
