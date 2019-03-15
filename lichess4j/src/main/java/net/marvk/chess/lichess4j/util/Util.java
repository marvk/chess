package net.marvk.chess.lichess4j.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Util {
    private Util() {
        throw new AssertionError("No instances of utility class " + Util.class);
    }

    public static String lichessApiToken(final Path path) {
        try {
            return String.join("\n", Files.readAllLines(path)).trim();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String lichessApiToken() {
        return lichessApiToken(Paths.get("lichess-api-token"));
    }
}
