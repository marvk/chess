package net.marvk.chess.engine;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

final class CommandUtil {
    private CommandUtil() {
        throw new AssertionError("No instances of utility class " + CommandUtil.class);
    }

    static <T> String toCommand(final String name, final T t) {
        return t == null ? "" : " " + name + " " + t;
    }

    static <T> String toCommand(final String name, final T t, final Function<T, String> stringMapper) {
        return t == null ? "" : " " + name + " " + stringMapper.apply(t);
    }

    static String toCommand(final String name, final UciMove[] uciMoves) {
        return toCommand(name, uciMoves, CommandUtil::convertToCommandString);
    }

    static String convertToCommandString(final UciMove[] moves) {
        //TODO
        throw new UnsupportedOperationException();
    }

    static String optionCommand(final String name, final String type) {
        Objects.requireNonNull(name);
        requireElseThrow(name, CommandUtil::isStringNotEmpty);

        return "option name " + name + " type " + type;
    }

    static <T> String optionCommand(final String name, final String type, final T defaultValue) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(defaultValue);
        requireElseThrow(name, CommandUtil::isStringNotEmpty);

        return "option name " + name + "type " + type + " default " + defaultValue;
    }

    static boolean isStringNotEmpty(final String string) {
        return !string.trim().isEmpty();
    }

    static <T> boolean requireElseThrow(final T t, final Predicate<T> predicate) {
        if (!predicate.test(t)) {
            throw new IllegalArgumentException(t.toString());
        }

        return true;
    }
}
