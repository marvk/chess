package net.marvk.chess.queensgambot;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.lichess4j.Client;
import net.marvk.chess.lichess4j.model.Perf;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Log4j2
public final class App {
    public static void main(final String[] args) {
        try (Client client = new Client("queensgambot", Perf.ULTRA_BULLET, Perf.BULLET)) {
            client.start();
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("", e);
        }
    }
}
