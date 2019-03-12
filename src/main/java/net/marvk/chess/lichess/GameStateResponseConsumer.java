package net.marvk.chess.lichess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.util.Util;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.function.BiConsumer;

@Log4j2
class GameStateResponseConsumer extends AsyncCharConsumer<Boolean> {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(GameState.class, new GameState.Deserializer())
                                                      .create();

    private final BiConsumer<GameState, String> gameStateConsumer;
    private final String gameId;

    GameStateResponseConsumer(final BiConsumer<GameState, String> gameStateConsumer, final String gameId) {
        this.gameStateConsumer = gameStateConsumer;
        this.gameId = gameId;
    }

    @Override
    protected void onCharReceived(final CharBuffer buf, final IOControl ioControl) throws IOException {
        final String response = Util.charBufferToString(buf).trim();

        if (response.matches("\\s*")) {
            log.trace("No new game state");
            return;
        }

        log.trace("Received game state response:\n" + response);

        final GameState gameState = GSON.fromJson(response, GameState.class);

        if (gameState == null) {
            log.warn("Received malformed game state:\n" + response);
        } else {
            log.info("Received event " + gameState);

            gameStateConsumer.accept(gameState, gameId);
        }
    }

    @Override
    protected void onResponseReceived(final HttpResponse httpResponse) throws HttpException, IOException {
    }

    @Override
    protected Boolean buildResult(final HttpContext httpContext) throws Exception {
        return Boolean.TRUE;
    }

}
