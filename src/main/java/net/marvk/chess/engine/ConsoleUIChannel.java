package net.marvk.chess.engine;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConsoleUIChannel implements UIChannel {
    private final PrintStream console;

    public ConsoleUIChannel(final PrintStream console) {
        this.console = console;
    }

    private void send(final String string) {
        console.println(string);
    }

    @Override
    public void idName(final String name) {
        Objects.requireNonNull(name);
        CommandUtil.requireElseThrow(name, CommandUtil::isStringNotEmpty);

        send("id name " + name);
    }

    @Override
    public void idAuthor(final String author) {
        Objects.requireNonNull(author);
        CommandUtil.requireElseThrow(author, s -> !s.isEmpty());

        send("id author " + author);
    }

    @Override
    public void uciOk() {
        send("uciok");
    }

    @Override
    public void readyOk() {
        send("readyok");
    }

    @Override
    public void bestMove(final UciMove move) {
        if (move == null) {
            send("bestmove 0000");
        } else {
            send("bestmove " + move.toString());
        }
    }

    @Override
    public void bestMove(final UciMove move, final UciMove ponder) {
        if (move == null) {
            send("bestmove 0000 ponder " + ponder.toString());
        } else {
            send("bestmove " + move.toString() + " ponder " + ponder.toString());
        }
    }

    @Override
    public void copyProtection() {
        send("copyprotection");
    }

    @Override
    public void registration() {
        send("registration");
    }

    @Override
    public void info(final Info info) {
        send(info.toCommand());
    }

    @Override
    public void optionCheck(final String name, final boolean defaultValue) {
        send(CommandUtil.optionCommand(name, "check", defaultValue));
    }

    @Override
    public void optionSpin(final String name, final int defaultValue, final int min, final int max) {
        send(CommandUtil.optionCommand(name, "spin", defaultValue) + " min " + min + " max " + max);
    }

    @Override
    public void optionCombo(final String name, final String defaultValue, final List<String> possibleValues) {
        CommandUtil.requireElseThrow(defaultValue, possibleValues::contains);
        CommandUtil.requireElseThrow(possibleValues, list -> !list.isEmpty());

        final String possibleValuesString =
                possibleValues.stream()
                              .map(s -> "var " + s)
                              .map(s -> s.replaceAll("\n", ""))
                              .collect(Collectors.joining(" "));

        send(CommandUtil.optionCommand(name, "combo", defaultValue) + " " + possibleValuesString);
    }

    @Override
    public void optionString(final String name, final String defaultValue) {
        send(CommandUtil.optionCommand(name, "string", defaultValue));
    }

    @Override
    public void optionButton(final String name) {
        send(CommandUtil.optionCommand(name, "button"));
    }

}
