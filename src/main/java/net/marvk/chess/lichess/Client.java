package net.marvk.chess.lichess;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.board.*;
import net.marvk.chess.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
public class Client implements AutoCloseable {
    private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
    private static final String HEADER_AUTHORIZATION_VALUE = "Bearer " + Util.lichessApiToken();
    private final CloseableHttpAsyncClient client;

    private final ExecutorService gameExecutor = Executors.newCachedThreadPool();

    public Client() {
        this.client = HttpAsyncClients.custom().setMaxConnTotal(100).build();
    }

    public void start() throws ExecutionException, InterruptedException {
        client.start();

        final HttpAsyncRequestProducer request = createAuthenticatedRequestProducer(Endpoints.eventStream());

        startEventHttpStream(request);
    }

    private void startEventHttpStream(final HttpAsyncRequestProducer request) throws InterruptedException, ExecutionException {
        final Future<Boolean> execute = client.execute(request, new EventResponseConsumer(this::acceptEvent), null);

        execute.get();
    }

    private void acceptEvent(final Event event) {
        if (event.getType() == Event.Type.CHALLENGE) {
            acceptChallenge(event.getId());
        } else {
            startGameHttpStream(event.getId());
        }
    }

    private void startGameHttpStream(final String gameId) {
        final HttpAsyncRequestProducer request = createAuthenticatedRequestProducer(Endpoints.gameStream(gameId));

        final Future<Boolean> callback = client.execute(request, new GameStateResponseConsumer(this::acceptGameState), null);

        gameExecutor.execute(() -> {
            try {
                callback.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e);
            }
        });
    }

    private void acceptChallenge(final String gameId) {
        final HttpUriRequest request = createAuthorizedPostRequest(Endpoints.acceptChallange(gameId));
        final Future<HttpResponse> callback = client.execute(request, null);

        try {
            final HttpResponse httpResponse = callback.get();

            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                log.info("Accepted challenge " + gameId);
            } else {
                log.warn("Failed to accept challenge " + gameId);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to accept challenge " + gameId, e);
        }
    }

    private void acceptGameState(final GameState gameState) {
        final Color activePlayer = gameState.getBoard().getState().getActivePlayer();
        final AlphaBetaPlayerExplicit player = new AlphaBetaPlayerExplicit(activePlayer, new SimpleHeuristic(), 4);
        final Move play = player.play(new MoveResult(gameState.getBoard(), Move.NULL_MOVE));

        final HttpUriRequest request = createAuthorizedPostRequest(Endpoints.makeMove(gameState.getId(), play));
        final Future<HttpResponse> callback = client.execute(request, null);

        try {
            final HttpResponse httpResponse = callback.get();

            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                log.info("Played move " + play + " in game " + gameState.getId());
            } else {
                log.warn("Failed to play move " + play + " in game " + gameState.getId());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to play move " + play + " in game " + gameState.getId(), e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    private static HttpUriRequest createAuthorizedPostRequest(final String url) {
        return RequestBuilder.post(url)
                             .addHeader(HEADER_AUTHORIZATION_KEY, HEADER_AUTHORIZATION_VALUE)
                             .build();
    }

    private static HttpAsyncRequestProducer createAuthenticatedRequestProducer(final String url) {
        final HttpUriRequest request =
                RequestBuilder.get(url)
                              .addHeader(HEADER_AUTHORIZATION_KEY, HEADER_AUTHORIZATION_VALUE)
                              .build();

        return HttpAsyncMethods.create(request);
    }

    public static void main(final String[] args) {
        try (Client client = new Client()) {
            client.start();
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error(e);
        }
    }
}
