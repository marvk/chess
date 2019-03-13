package net.marvk.chess.lichess;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.board.AlphaBetaPlayerExplicit;
import net.marvk.chess.board.PlayerFactory;
import net.marvk.chess.board.SimpleHeuristic;
import net.marvk.chess.lichess.model.Challenge;
import net.marvk.chess.lichess.model.GameStart;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
public class Client implements AutoCloseable {
    private final CloseableHttpAsyncClient asyncClient;
    private final CloseableHttpClient httpClient;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final String lichessBotId;
    public static final PlayerFactory PLAYER_FACTORY = c -> new AlphaBetaPlayerExplicit(c, new SimpleHeuristic(), 3);

    public Client(final String lichessBotId) throws IOReactorException {
        this.lichessBotId = lichessBotId;

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(10).build();
        final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        final NHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);

        this.httpClient = HttpClientBuilder.create().build();

        this.asyncClient = HttpAsyncClients.custom()
                                           .setConnectionManager(connectionManager)
                                           .setMaxConnTotal(100)
                                           .build();
    }

    public void start() throws ExecutionException, InterruptedException {
        asyncClient.start();

        final HttpAsyncRequestProducer request = HttpUtil.createAuthenticatedRequestProducer(Endpoints.eventStream());

        startEventHttpStream(request);
    }

    private void startEventHttpStream(final HttpAsyncRequestProducer request) throws InterruptedException, ExecutionException {
        log.info("Starting event stream");

        final Future<Boolean> execute = asyncClient.execute(request, new EventResponseConsumer(this::acceptChallenge, this::startGameHttpStream), null);

        execute.get();
        log.info("Closing event stream");
    }

    private void acceptChallenge(final Challenge challenge) {
        final String gameId = challenge.getId();

        executor.execute(() -> {
            final HttpUriRequest request = HttpUtil.createAuthorizedPostRequest(Endpoints.acceptChallenge(gameId));

            log.info("Trying to accept challenge " + gameId + "...");

            try (final CloseableHttpResponse httpResponse = httpClient.execute(request)) {

                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    log.info("Accepted challenge " + gameId);
                } else {
                    log.warn("Failed to accept challenge " + gameId);
                }

                EntityUtils.consume(httpResponse.getEntity());

                log.debug("Consumed entity");
            } catch (final ClientProtocolException e) {
                log.error("Failed to accept challenge " + gameId, e);
            } catch (final IOException e) {
                log.error("", e);
            }
        });
    }

    private void startGameHttpStream(final GameStart gameStart) {
        executor.execute(new GameThread(gameStart.getId(), httpClient, executor, lichessBotId, PLAYER_FACTORY));
    }

    @Override
    public void close() throws IOException {
        asyncClient.close();
        httpClient.close();
    }

    public static void main(final String[] args) {
        try (Client client = new Client("queensgambot")) {
            client.start();
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("", e);
        }
    }
}
