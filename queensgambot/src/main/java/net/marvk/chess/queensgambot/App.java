package net.marvk.chess.queensgambot;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.kairukuengine.Kairuku;
import net.marvk.chess.lichess4j.LichessClient;
import net.marvk.chess.lichess4j.LichessClientBuilder;
import net.marvk.chess.lichess4j.model.Perf;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

@Log4j2
public final class App {
    public static void main(final String[] args) throws IOException {
        try (final LichessClient client =
                     LichessClientBuilder.create("queensgambot", Kairuku::new)
                                         .allowPerf(Perf.BULLET)
                                         .apiTokenFromPath(Paths.get("lichess-api-token"))
                                         .build()
        ) {
            client.start();
        } catch (InterruptedException | ExecutionException e) {
            log.error("", e);
        }
    }
}
