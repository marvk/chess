package net.marvk.chess.lichess4j;

import lombok.Data;
import lombok.NonNull;
import net.marvk.chess.lichess4j.model.GameState;
import net.marvk.chess.lichess4j.model.GameStateFull;
import net.marvk.chess.lichess4j.model.Room;
import net.marvk.chess.uci4j.UciEngine;

import java.util.function.Consumer;

@Data
public class LichessChatContext {
    private final @NonNull Consumer<LichessChatResponse> responseConsumer;

    private final @NonNull UciEngine engine;
    private final @NonNull GameStateFull initialGameState;
    private final @NonNull GameState lastGameState;

    public void accept(final LichessChatResponse response) {
        responseConsumer.accept(response);
    }

    public void accept(final Room room, final String message) {
        responseConsumer.accept(new LichessChatResponse(room, message));
    }
}
