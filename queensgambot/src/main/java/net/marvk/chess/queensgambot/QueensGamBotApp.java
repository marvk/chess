package net.marvk.chess.queensgambot;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.kairukuengine.KairukuEngine;
import net.marvk.chess.kairukuengine.Metrics;
import net.marvk.chess.lichess4j.LichessChatContext;
import net.marvk.chess.lichess4j.LichessClient;
import net.marvk.chess.lichess4j.LichessClientBuilder;
import net.marvk.chess.lichess4j.model.ChatLine;
import net.marvk.chess.lichess4j.model.Perf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;

@Log4j2
public final class QueensGamBotApp {
    private static final Path API_KEY_PATH = Paths.get("lichess-api-token");

    private QueensGamBotApp() {
        throw new AssertionError("No instances of utility class " + QueensGamBotApp.class);
    }

    public static void main(final String[] args) throws IOException {
        log.info(System.getenv());

        final String lichessApiToken;

        if (Files.exists(API_KEY_PATH)) {
            lichessApiToken = String.join("\n", Files.readAllLines(API_KEY_PATH)).trim();
        } else {
            log.warn("Failed to read api key from file, trying to read from env key LICHESS_API_TOKEN");

            final String envToken = System.getenv("LICHESS_API_TOKEN");

            if (envToken == null) {
                throw new IllegalStateException("No API key");
            }

            lichessApiToken = envToken;
        }

        try (final LichessClient client =
                     LichessClientBuilder.create("queensgambot", KairukuEngine::new)
                                         .allowAllPerfs(Perf.BULLET, Perf.BLITZ)
                                         .allowAllPerfsOnCasual(true)
                                         .apiToken(lichessApiToken)
                                         .eventHandlerWithPrefixes(QueensGamBotApp::infoString, "!li", "!lm", "!lastinfo", "lastmetrics")
                                         .build()
        ) {
            client.start();
        } catch (InterruptedException | ExecutionException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("", e);
        }
    }

    private static String pretty(final Metrics metrics) {
        final StringJoiner result = new StringJoiner("\n");

        result.add("NPS: " + metrics.getLastNps());
        result.add("Nodes: " + metrics.getLastNodes());
        result.add("Duration: " + metrics.getLastDuration());
        result.add("Avg. Q termination: " + metrics.getLastAverageQuiescenceTerminationDepth());

        return result.toString();
    }

    private static void infoString(final ChatLine chatLine, final LichessChatContext ctx) {
        ctx.accept(chatLine.getRoom(), pretty(((KairukuEngine) ctx.getEngine()).getMetrics()));
    }
}
