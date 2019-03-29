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
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;

@Log4j2
public final class QueensGamBotApp {
    private QueensGamBotApp() {
        throw new AssertionError("No instances of utility class " + QueensGamBotApp.class);
    }

    public static void main(final String[] args) throws IOException {
        try (final LichessClient client =
                     LichessClientBuilder.create("queensgambot", KairukuEngine::new)
                                         .allowPerf(Perf.BULLET)
                                         .apiTokenFromPath(Paths.get("lichess-api-token"))
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
