package net.marvk.chess.lichess4j;

import net.marvk.chess.lichess4j.model.ChatLine;
import net.marvk.chess.lichess4j.model.Room;

import java.util.ArrayList;
import java.util.Collection;

public final class CompositeChatMessageEventHandler implements ChatMessageEventHandler {
    private final Collection<ChatMessageEventHandler> eventHandlers;

    private CompositeChatMessageEventHandler(final Collection<ChatMessageEventHandler> eventHandlers) {
        this.eventHandlers = new ArrayList<>(eventHandlers);
    }

    @Override
    public void accept(final ChatLine chatLine, final LichessChatContext ctx) {
        eventHandlers.forEach(e -> e.accept(chatLine, ctx));
    }

    public static ChatMessageEventHandler of(final Collection<ChatMessageEventHandler> eventHandlers) {
        if (eventHandlers.size() < 1) {
            return ChatMessageEventHandler.NULL_HANDLER;
        }

        if (eventHandlers.size() == 1) {
            return eventHandlers.stream().findFirst().get();
        }

        return new CompositeChatMessageEventHandler(eventHandlers);
    }
}
