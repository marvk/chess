package net.marvk.chess.lichess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.lichess.model.*;
import net.marvk.chess.lichess.serialization.GameStateResponseDeserializer;
import net.marvk.chess.lichess.serialization.LocalDateTimeDeserializer;
import net.marvk.chess.lichess.serialization.UciMoveArrayDeserializer;
import net.marvk.chess.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.protocol.HttpContext;

import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Log4j2
class GameStateResponseConsumer extends AsyncCharConsumer<Boolean> {
    private static final Gson GSON =
            new GsonBuilder().registerTypeAdapter(UciMove[].class, new UciMoveArrayDeserializer())
                             .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                             .registerTypeAdapter(GameStateResponse.class, new GameStateResponseDeserializer())
                             .create();

    private final Consumer<GameStateFull> gameStateFullConsumer;
    private final Consumer<GameState> gameStateConsumer;
    private final Consumer<ChatLine> chatLineConsumer;

    GameStateResponseConsumer(
            final Consumer<GameStateFull> gameStateFullConsumer,
            final Consumer<GameState> gameStateConsumer,
            final Consumer<ChatLine> chatLineConsumer
    ) {
        this.gameStateFullConsumer = gameStateFullConsumer;
        this.gameStateConsumer = gameStateConsumer;
        this.chatLineConsumer = chatLineConsumer;
    }

    @Override
    protected void onCharReceived(final CharBuffer buf, final IOControl ioControl) {
        final String response = Util.charBufferToString(buf).trim();

        if (response.matches("\\s*")) {
            log.trace("No new game state");
            return;
        }

        log.trace("Received game state response:\n" + response);

        final GameStateResponse gameStateResponse = GSON.fromJson(response, GameStateResponse.class);

        if (gameStateResponse != null) {
            acceptGameStateResponse(gameStateResponse);
        }
    }

    private void acceptGameStateResponse(final GameStateResponse gameStateResponse) {
        if (gameStateResponse == null) {
            log.warn("Received null game state response");
            return;
        }

        final GameStateResponse.Type type = gameStateResponse.getType();

        if (type == GameStateResponse.Type.GAME_FULL) {
            final GameStateFull gameStateFull = gameStateResponse.getGameStateFull();
            log.info("Received full game state: " + gameStateFull);

            gameStateFullConsumer.accept(gameStateFull);
        } else if (type == GameStateResponse.Type.GAME_STATE) {
            final GameState gameState = gameStateResponse.getGameState();
            log.info("Received game state: " + gameState);

            gameStateConsumer.accept(gameState);
        } else if (type == GameStateResponse.Type.CHAT_LINE) {
            final ChatLine chatLine = gameStateResponse.getChatLine();
            log.info("Received chat line: " + chatLine);

            chatLineConsumer.accept(chatLine);
        } else {
            log.warn("Received malformed game state response: " + gameStateResponse);
        }
    }

    @Override
    protected void onResponseReceived(final HttpResponse httpResponse) {
    }

    @Override
    protected Boolean buildResult(final HttpContext httpContext) {
        return Boolean.TRUE;
    }

}
