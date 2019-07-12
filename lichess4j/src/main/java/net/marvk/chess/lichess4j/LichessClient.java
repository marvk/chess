package net.marvk.chess.lichess4j;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.lichess4j.model.Challenge;
import net.marvk.chess.lichess4j.model.GameStart;
import net.marvk.chess.lichess4j.model.Perf;
import net.marvk.chess.lichess4j.util.HttpUtil;
import net.marvk.chess.uci4j.EngineFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
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
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class LichessClient implements AutoCloseable {
    private final CloseableHttpAsyncClient asyncClient;
    private final CloseableHttpClient httpClient;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Set<Perf> allowedPerfs;
    private final boolean allowAllPerfsOnCasual;
    private final EngineFactory engineFactory;
    private final ChatMessageEventHandler eventHandler;
    private final String accountName;
    private final String apiToken;

    LichessClient(final String accountName, final String apiToken, final Collection<Perf> allowedPerfs, final boolean allowAllPerfsOnCasual, final EngineFactory engineFactory, final ChatMessageEventHandler eventHandler) throws LichessClientInstantiationException {
        this.accountName = accountName;
        this.apiToken = apiToken;
        this.allowedPerfs = Set.copyOf(allowedPerfs);
        this.allowAllPerfsOnCasual = allowAllPerfsOnCasual;
        this.engineFactory = engineFactory;
        this.eventHandler = eventHandler;

        final SSLContext build = createSslContext();

        this.httpClient = HttpClientBuilder.create()
                                           .setSSLContext(build)
                                           .setSSLHostnameVerifier(new NoopHostnameVerifier())
                                           .build();

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(10).build();
        final ConnectingIOReactor ioReactor = createIoReactor(ioReactorConfig);
        final NHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);

        this.asyncClient = HttpAsyncClients.custom()
                                           .setConnectionManager(connectionManager)
                                           .setMaxConnTotal(100)
                                           .build();
    }

    private static SSLContext createSslContext() throws LichessClientInstantiationException {
        try {
            return new SSLContextBuilder().loadTrustMaterial(null, (c, at) -> true).build();
        } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new LichessClientInstantiationException(e);
        }
    }

    private static ConnectingIOReactor createIoReactor(final IOReactorConfig ioReactorConfig) throws LichessClientInstantiationException {
        try {
            return new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (final IOReactorException e) {
            throw new LichessClientInstantiationException(e);
        }
    }

    public void start() throws LichessClientOperationException {
        asyncClient.start();

        final HttpAsyncRequestProducer request = HttpUtil.createAuthenticatedRequestProducer(Endpoints.eventStream(), apiToken);

        try {
            startEventHttpStream(request);
        } catch (final InterruptedException | ExecutionException e) {
            throw new LichessClientOperationException(e);
        }
    }

    private void startEventHttpStream(final HttpAsyncRequestProducer request) throws InterruptedException, ExecutionException {
        log.info("Starting event stream");

        final Future<Boolean> execute = asyncClient.execute(request, new EventResponseConsumer(this::handleChallenge, this::startGameHttpStream), null);

        execute.get();
        log.info("Closing event stream");
    }

    private void handleChallenge(final Challenge challenge) {
        final String gameId = challenge.getId();
        final Perf perf = challenge.getPerf();

        final String endpoint;

        final String rated = challenge.getRated() ? "rated" : "casual";

        if ((allowAllPerfsOnCasual && !challenge.getRated()) || allowedPerfs.contains(perf)) {
            log.info("Accepting " + rated + " challenge " + gameId + " with perf " + perf);
            endpoint = Endpoints.acceptChallenge(gameId);
        } else {
            log.info("Declining " + rated + " challenge " + challenge.getId() + " due to perf mismatch, allowed perfs are " + allowedPerfs + " but got " + perf);
            endpoint = Endpoints.declineChallenge(gameId);
        }

        executor.execute(() -> {
            final HttpUriRequest request = HttpUtil.createAuthorizedPostRequest(endpoint, apiToken);

            log.trace("Trying to handle challenge " + gameId + "...");

            try (final CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                final HttpEntity entity = httpResponse.getEntity();

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    log.info("Handled challenge " + gameId);
                } else {
                    log.warn("Failed to handle challenge " + gameId + ": " + EntityUtils.toString(entity));
                }

                EntityUtils.consume(entity);

                log.trace("Consumed entity");
            } catch (final ClientProtocolException e) {
                log.error("Failed to handle challenge " + gameId, e);
            } catch (final IOException e) {
                log.error("", e);
            }
        });
    }

    private void startGameHttpStream(final GameStart gameStart) {
        executor.execute(new GameThread(accountName, apiToken, gameStart.getId(), httpClient, executor, engineFactory, eventHandler));
    }

    @Override
    public void close() throws IOException {
        asyncClient.close();
        httpClient.close();
    }

    private static List<Perf> mergePerfsList(final Perf perf, final Perf[] perfs) {
        return Stream.concat(Stream.of(perf), Arrays.stream(perfs)).collect(Collectors.toList());
    }
}
