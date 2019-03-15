package net.marvk.chess.uci4j;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.board.UciMove;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.function.Function;

@Log4j2
public class ConsoleEngineChannel implements AutoCloseable {
    private static final UciMove[] UCI_MOVES_EMPTY = new UciMove[0];
    private final Engine engine;
    private final Scanner scanner;

    public ConsoleEngineChannel(final Engine engine, final InputStream inputStream) {
        this.engine = engine;
        this.scanner = new Scanner(inputStream);
    }

    public void start() {
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();

            final CommandParser commandParser = new CommandParser(line.trim());

            log.info(line);

            switch (commandParser.getCommand()) {
                case "uci":
                    engine.uci();
                    break;
                case "debug":
                    debug(commandParser);
                    break;
                case "isready":
                    engine.isReady();
                    break;
                case "setoption":
                    setOption(commandParser);
                    break;
                case "register":
                    register(commandParser);
                    break;
                case "ucinewgame":
                    engine.uciNewGame();
                    break;
                case "position":
                    position(commandParser);
                    break;
                case "go":
                    go(commandParser);
                    break;
                case "stop":
                    engine.stop();
                    break;
                case "ponderhit":
                    engine.ponderHit();
                case "quit":
                    engine.quit();
            }
        }
    }

    private void go(final CommandParser commandParser) {
        final UciMove[] searchMoves = movesFromStringSafe(commandParser.getParameter("searchmoves"));
        final boolean ponder = commandParser.containsParameter("ponder");
        final Integer whiteTime = commandParser.getParameter("wtime", Integer::parseInt);
        final Integer blackTime = commandParser.getParameter("btime", Integer::parseInt);
        final Integer whiteIncrement = commandParser.getParameter("winc", Integer::parseInt);
        final Integer blackIncrement = commandParser.getParameter("binc", Integer::parseInt);
        final Integer movesToGo = commandParser.getParameter("movestogo", Integer::parseInt);
        final Integer depth = commandParser.getParameter("depth", Integer::parseInt);
        final Integer nodes = commandParser.getParameter("nodes", Integer::parseInt);
        final Integer mate = commandParser.getParameter("mate", Integer::parseInt);
        final Integer moveTime = commandParser.getParameter("movetime", Integer::parseInt);
        final boolean infinite = commandParser.containsParameter("infinite");

        final Go go = new Go(searchMoves, ponder, whiteTime, blackTime, whiteIncrement, blackIncrement, movesToGo, depth, nodes, mate, moveTime, infinite);

        engine.go(go);
    }

    private void position(final CommandParser commandParser) {
        final String fen = commandParser.getParameter("fen", "moves");
        final UciMove[] moves = movesFromStringSafe(commandParser.getParameter("moves"));

        if ("startpos".equals(fen)) {
            engine.positionFromDefault(moves);
        } else {
            engine.position(fen, moves);
        }
    }

    private static UciMove[] movesFromStringSafe(final String moves) {
        if (moves == null || moves.isEmpty()) {
            return UCI_MOVES_EMPTY;
        } else {
            return UciMove.parseLine(moves);
        }
    }

    private void register(final CommandParser commandParser) {
        if (commandParser.containsParameter("later")) {
            engine.registerLater();
        } else {
            final String name = commandParser.getParameter("name");
            final String code = commandParser.getParameter("code");

            if (name != null && code != null) {
                engine.register(name, code);
            }
        }
    }

    private void debug(final CommandParser commandParser) {
        if (commandParser.containsParameter("on")) {
            engine.setDebug(true);
        } else if (commandParser.containsParameter("off")) {
            engine.setDebug(false);
        }
    }

    private void isReady() {
        engine.isReady();
    }

    private void setOption(final CommandParser commandParser) {
        final String name = commandParser.getParameter("name");
        final String value = commandParser.getParameter("value");

        if (name != null) {
            engine.setOption(name, value);
        }
    }

    private static class CommandParser {

        private final String[] split;

        public CommandParser(final String line) {
            this.split = line.split("\\s+");
        }

        public String getCommand() {
            if (split.length == 0) {
                return null;
            }

            return split[0];
        }

        public boolean containsParameter(final String string) {
            return Arrays.asList(split).contains(string);
        }

        public String getParameter(final String name) {

            for (int i = 1; i < split.length - 1; i++) {
                if (split[i].equals(name)) {
                    return split[i + 1];
                }
            }

            return null;
        }

        public String getParameter(final String name, final String until) {

            for (int i = 1; i < split.length - 1; i++) {
                if (split[i].equals(name)) {
                    final StringJoiner parameter = new StringJoiner(" ");

                    for (int j = i + 1; j < split.length; j++) {
                        if (split[j].equals(until)) {
                            break;
                        }

                        parameter.add(split[j]);
                    }

                    return parameter.length() == 0 ? null : parameter.toString();
                }
            }

            return null;
        }

        private <T> T getParameter(final String name, final Function<String, T> mapping) {
            final String parameter = getParameter(name);

            if (parameter == null) {
                return null;
            }

            return mapping.apply(parameter);
        }
    }

    @Override
    public void close() {
        scanner.close();
    }
}
