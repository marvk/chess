package net.marvk.chess.uci4j;

import net.marvk.chess.core.UciMove;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExecutableUciEngine extends SimpleUciEngine {
    private final PrintStream engineChannel;

    public ExecutableUciEngine(final UiChannel uiChannel, final InputStream inputStream, final OutputStream outputStream) {
        super(uiChannel);

        this.engineChannel = new PrintStream(outputStream);

        try (final ConsoleEngineChannel consoleEngineChannel = new ConsoleEngineChannel(this, inputStream)) {
            consoleEngineChannel.start();
        }
    }

    @Override
    public void uci() {
        engineChannel.println("uci");
    }

    @Override
    public void setDebug(final boolean debug) {
        engineChannel.println("debug " + (debug ? "on" : "off"));
    }

    @Override
    public void isReady() {
        engineChannel.println("isready");
    }

    @Override
    public void setOption(final String name, final String value) {
        engineChannel.println("setoption " + name + " " + value);
    }

    @Override
    public void registerLater() {
        engineChannel.println("register later");
    }

    @Override
    public void register(final String name, final String code) {
        engineChannel.println("register name " + name + " code " + code);
    }

    @Override
    public void uciNewGame() {
        engineChannel.println("ucinewgame");
    }

    @Override
    public void positionFromDefault(final UciMove[] moves) {
        engineChannel.println("position startpos " + uciString(moves));
    }

    @Override
    public void position(final String fenString, final UciMove[] moves) {
        engineChannel.println("position " + fenString + " " + uciString(moves));
    }

    @Override
    public void go(final Go go) {
        final StringJoiner sj = new StringJoiner("\n");

        append(sj, "searchmoves", go.getSearchMoves(), ExecutableUciEngine::uciString);
        append(sj, "ponder", go.getPonder());
        append(sj, "wtime", go.getWhiteTime());
        append(sj, "btime", go.getBlackTime());
        append(sj, "winc", go.getWhiteIncrement());
        append(sj, "binc", go.getBlackIncrement());
        append(sj, "movestogo", go.getMovesToGo());
        append(sj, "depth", go.getDepth());
        append(sj, "nodes", go.getNodes());
        append(sj, "mate", go.getMate());
        append(sj, "movetime", go.getMoveTime());
        append(sj, "infinite", go.getInfinite());

        engineChannel.println(sj.toString());
    }

    @Override
    public void stop() {
        engineChannel.println("stop");
    }

    @Override
    public void ponderHit() {
        engineChannel.println("ponderhit");
    }

    @Override
    public void quit() {
        engineChannel.println("quit");
    }

    private static void append(
            final StringJoiner sj,
            final String valueName,
            final Boolean append
    ) {
        if (append != null && append) {
            sj.add(valueName);
        }
    }

    private static <E> void append(
            final StringJoiner sj,
            final String valueName,
            final E e
    ) {
        append(sj, valueName, e, E::toString);
    }

    private static <E> void append(
            final StringJoiner sj,
            final String valueName,
            final E e,
            final Function<E, String> mapper
    ) {
        if (e != null) {
            sj.add(valueName + " " + mapper.apply(e));
        }
    }

    private static String uciString(final UciMove[] moves) {
        return Arrays.stream(moves).map(UciMove::toString).collect(Collectors.joining(" "));
    }
}
