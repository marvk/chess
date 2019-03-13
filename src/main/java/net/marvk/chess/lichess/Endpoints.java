package net.marvk.chess.lichess;

import net.marvk.chess.board.Move;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class Endpoints {
    private static final String URL = "https://lichess.org";

    private Endpoints() {
        throw new AssertionError("No instances of utility class " + Endpoints.class);
    }

    public static String url() {
        return URL;
    }

    public static String eventStream() {
        return URL + "/api/stream/event";
    }

    public static String acceptChallenge(final String gameId) {
        return URL + "/api/challenge/" + gameId + "/accept";
    }

    public static String declineChallenge(final String gameId) {
        return URL + "/api/challenge/" + gameId + "/decline";
    }

    public static String makeMove(final String gameId, final Move move) {
        return URL + "/api/bot/game/" + gameId + "/move/" + move.getUci();
    }

    public static String gameStream(final String gameId) {
        return URL + "/api/bot/game/stream/" + gameId;
    }

    public static String writeInChat(final String gameId, final Room room, final String text) {
        try {
            return URL + "/api/bot/game/" + gameId + "/chat?room=" + room.getRepresentation() + "&text=" + URLEncoder.encode(text, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
