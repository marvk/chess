package net.marvk.chess.lichess;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.board.*;
import net.marvk.chess.lichess.model.ChatLine;
import net.marvk.chess.lichess.model.GameState;
import net.marvk.chess.lichess.model.GameStateFull;
import net.marvk.chess.lichess.model.UciMove;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Log4j2
public class GameThread implements Runnable {
    private final String gameId;
    private final CloseableHttpClient httpClient;
    private final ExecutorService executorService;
    private final String botId;
    private final PlayerFactory playerFactory;

    private Color myColor;
    private Player player;

    public GameThread(final String gameId, final CloseableHttpClient httpClient, final ExecutorService executorService, final String botId, final PlayerFactory playerFactory) {
        this.gameId = gameId;
        this.httpClient = httpClient;
        this.executorService = executorService;
        this.botId = botId;
        this.playerFactory = playerFactory;
    }

    public void acceptFullGameState(final GameStateFull gameStateFull) {
        if (botId.equals(gameStateFull.getWhite().getId())) {
            this.myColor = Color.WHITE;
        } else if (botId.equals(gameStateFull.getBlack().getId())) {
            this.myColor = Color.BLACK;
        }

        this.player = playerFactory.create(myColor);

        log.info("Bot color set to " + myColor + " in game " + gameId);

        this.acceptGameState(gameStateFull.getGameState());
    }

    public void acceptGameState(final GameState gameState) {
        final Board board = UciMove.getBoard(gameState.getMoves());

        if (board.getState().getActivePlayer() != myColor) {
            log.debug("Not calculating move for opponent");
            return;
        }

        executorService.execute(() -> {
            final Move play = player.play(new MoveResult(board, Move.NULL_MOVE));

            final HttpUriRequest request = HttpUtil.createAuthorizedPostRequest(Endpoints.makeMove(gameId, play));

            log.info("Trying to play move " + play + " in game " + gameId + "...");
            try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {

                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    log.info("Played move " + play + " in game " + gameId);
                } else {
                    log.warn("Failed to play move " + play + " in game " + gameId);
                }

                EntityUtils.consume(httpResponse.getEntity());

                log.debug("Consumed entity");
            } catch (final ClientProtocolException e) {
                log.error("Failed to play move " + play + " in game " + gameId, e);
            } catch (final IOException e) {
                log.error("", e);
            }
        });
    }

    public void acceptChatLine(final ChatLine chatLine) {

    }

    @Override
    public void run() {
        log.info("Starting stream for game " + gameId);
        try (final CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();

            final HttpAsyncRequestProducer request = HttpUtil.createAuthenticatedRequestProducer(Endpoints.gameStream(gameId));

            final Future<Boolean> callback = client.execute(
                    request,
                    new GameStateResponseConsumer(
                            this::acceptFullGameState,
                            this::acceptGameState,
                            this::acceptChatLine
                    ),
                    null
            );

            try {
                callback.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("", e);
            }
        } catch (final IOException e) {
            log.error("", e);
        }

        log.info("Closing stream for game " + gameId);
    }
}
