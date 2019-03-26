package net.marvk.chess.lichess4j;

import net.marvk.chess.lichess4j.model.ChatLine;

class PrefixChatMessageEventHandler implements ChatMessageEventHandler {
    private final String prefix;
    private final ChatMessageEventHandler eventHandler;

    PrefixChatMessageEventHandler(final String prefix, final ChatMessageEventHandler eventHandler) {
        this.prefix = prefix;
        this.eventHandler = eventHandler;
    }

    @Override
    public void accept(final ChatLine chatLine, final LichessChatContext ctx) {
        if (chatLine.getText().startsWith(prefix)) {
            eventHandler.accept(chatLine, ctx);
        }
    }
}
