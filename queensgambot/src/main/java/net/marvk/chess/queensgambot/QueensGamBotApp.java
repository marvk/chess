package net.marvk.chess.queensgambot;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.kairukuengine.KairukuEngine;
import net.marvk.chess.kairukuengine.Metrics;
import net.marvk.chess.lichess4j.*;
import net.marvk.chess.lichess4j.model.ChatLine;
import net.marvk.chess.lichess4j.model.Perf;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

@Log4j2
public final class QueensGamBotApp {
    private static final Path API_TOKEN_PATH = Paths.get("lichess-api-token");
    private static final String API_TOKEN_ENV_KEY = "LICHESS_API_TOKEN";
    private static final Option API_TOKEN_OPTION = Option.builder("t")
                                                         .hasArg()
                                                         .required(false)
                                                         .longOpt("lichessApiToken")
                                                         .argName("Lichess API Token")
                                                         .desc("The API token for lichess.org")
                                                         .build();

    private QueensGamBotApp() {
        throw new AssertionError("No instances of utility class " + QueensGamBotApp.class);
    }

    public static void main(final String[] args) throws IOException, ParseException {
        final String lichessApiToken = getApiToken(args);

        try (final LichessClient client =
                     LichessClientBuilder.create("queensgambot", KairukuEngine::new)
                                         .allowAllPerfs(Perf.BULLET, Perf.BLITZ)
                                         .allowAllPerfsOnCasual(true)
                                         .apiToken(lichessApiToken)
                                         .eventHandlerWithPrefixes(QueensGamBotApp::infoString, "!li", "!lm", "!lastinfo", "lastmetrics")
                                         .build()
        ) {
            client.start();
        } catch (final LichessClientInstantiationException | LichessClientOperationException e) {
            log.error("", e);
        }
    }

    private static CommandLine getCommandLineArgs(final String[] args) throws ParseException {
        final CommandLineParser defaultParser = new DefaultParser();

        final Options options = new Options();
        options.addOption(API_TOKEN_OPTION);

        return defaultParser.parse(options, args);
    }

    private static String getApiToken(final String[] args) throws IOException, ParseException {
        final CommandLine commandLine = getCommandLineArgs(args);
        if (commandLine.hasOption(API_TOKEN_OPTION.getOpt())) {
            return commandLine.getOptionValue(API_TOKEN_OPTION.getOpt());
        } else if (Files.exists(API_TOKEN_PATH)) {
            return String.join("\n", Files.readAllLines(API_TOKEN_PATH)).trim();
        } else {
            log.warn("Failed to read api token from file " + API_TOKEN_PATH + ", trying to read from environment variable " + API_TOKEN_ENV_KEY);

            final String envToken = System.getenv(API_TOKEN_ENV_KEY);

            if (envToken == null) {
                final String message = "No API token specified. Please specify an API token either by setting the -k command line argument, by providing a file named "
                        + API_TOKEN_PATH.getFileName()
                        + " on the classpath or by setting the environment variable "
                        + API_TOKEN_ENV_KEY;

                throw new IllegalStateException(message);
            }

            return envToken;
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
