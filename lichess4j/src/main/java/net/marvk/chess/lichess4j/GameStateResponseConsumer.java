package net.marvk.chess.lichess4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.UciMove;
import net.marvk.chess.lichess4j.model.*;
import net.marvk.chess.lichess4j.serialization.GameStateResponseDeserializer;
import net.marvk.chess.lichess4j.serialization.LocalDateTimeDeserializer;
import net.marvk.chess.lichess4j.serialization.PerfDeserializer;
import net.marvk.chess.lichess4j.serialization.UciMoveArrayDeserializer;
import net.marvk.chess.lichess4j.util.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.protocol.HttpContext;

import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Consumer;

@Log4j2
class GameStateResponseConsumer extends AsyncCharConsumer<Boolean> {
    private static final Gson GSON =
            new GsonBuilder().registerTypeAdapter(UciMove[].class, new UciMoveArrayDeserializer())
                             .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                             .registerTypeAdapter(GameStateResponse.class, new GameStateResponseDeserializer())
                             .registerTypeAdapter(Perf.class, new PerfDeserializer())
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
        final String response = HttpUtil.charBufferToString(buf).trim();

        if (response.isBlank()) {
            log.trace("No new game state");
            return;
        }

        log.trace("Received game state response:\n" + response);

        Arrays.stream(response.split("\n"))
              .map(GameStateResponseConsumer::safeJson)
              .forEach(this::acceptGameStateResponse);
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

    private static GameStateResponse safeJson(final String line) {
        try {
            return GSON.fromJson(line, GameStateResponse.class);
        } catch (final JsonParseException e) {
            log.error("Failed to parse line:\n " + line, e);
            return null;
        }
    }
}
