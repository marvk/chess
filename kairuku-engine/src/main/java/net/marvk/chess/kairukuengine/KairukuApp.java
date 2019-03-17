package net.marvk.chess.kairukuengine;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.uci4j.ConsoleEngineChannel;
import net.marvk.chess.uci4j.ConsoleUIChannel;
import net.marvk.chess.uci4j.UciEngine;

@Log4j2
public final class KairukuApp {
    private KairukuApp() {
        throw new AssertionError("No instances of main class " + KairukuApp.class);
    }

    public static void main(final String[] args) {
        final UciEngine engine = new KairukuEngine(new ConsoleUIChannel(s -> {
            log.info(s);
            System.out.println(s);
        }));

        try (final ConsoleEngineChannel consoleEngineChannel = new ConsoleEngineChannel(engine, System.in)) {
            consoleEngineChannel.start();
        }
    }
}
