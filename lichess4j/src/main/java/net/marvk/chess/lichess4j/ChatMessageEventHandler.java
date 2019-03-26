package net.marvk.chess.lichess4j;

import net.marvk.chess.lichess4j.model.ChatLine;

@FunctionalInterface
public interface ChatMessageEventHandler {
    ChatMessageEventHandler NULL_HANDLER = (chatLine, ctx) -> {
    };

    void accept(final ChatLine chatLine, final LichessChatContext ctx);
}
