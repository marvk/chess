package net.marvk.chess.engine;

import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

@Log4j2
public class EngineApp {
    public static void main(final String[] args) {
        final Engine engine = new EnginePlayerAdapter(new ConsoleUIChannel(s -> {
            log.info(s);
            System.out.println(s);
        }));

        try (final ConsoleEngineChannel consoleEngineChannel = new ConsoleEngineChannel(engine, System.in)) {
            consoleEngineChannel.start();
        }
    }
}
