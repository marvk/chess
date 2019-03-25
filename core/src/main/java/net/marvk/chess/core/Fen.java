package net.marvk.chess.core;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fen {
    private static final Pattern FEN_PATTERN = Pattern.compile("^" +
            "(?<piecePlacement>(?!.{0,6}[1-8][1-8])[PNBRQKpnbrqk1-8]{1,8}(?:/(?!.{0,6}[1-8][1-8])[PNBRQKpnbrqk1-8]{1,8}){7}) " +
            "(?<activeColor>[bw]) " +
            "(?<castlingAvailability>(?=[KQkq])(?:K?Q?k?q?)|-) " +
            "(?<enPassantTargetSquare>[a-h][1-8]|-)(:? " +
            "(?<halfmoveClock>\\d+) " +
            "(?<fullmoveClock>\\d+))?" +
            "$"
    );

    public static final Fen EMPTY_BOARD = Fen.parse("8/8/8/8/8/8/8/8 w - -");
    public static final Fen STARTING_POSITION = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

    private final String input;
    private final String piecePlacement;
    private final String activeColor;
    private final String castlingAvailability;
    private final String enPassantTargetSquare;
    private final String halfmoveClock;
    private final String fullmoveClock;

    private Fen(final String input) {
        this(input, FEN_PATTERN.matcher(input));

        if (!isValid()) {
            throw new IllegalArgumentException("Input string is not a valid FEN notation");
        }
    }

    private Fen(final String input, final Matcher matcher) {
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Input string is not a valid FEN notation: " + input);
        }

        this.input = input;
        this.piecePlacement = matcher.group("piecePlacement");
        this.activeColor = matcher.group("activeColor");
        this.castlingAvailability = matcher.group("castlingAvailability");
        this.enPassantTargetSquare = matcher.group("enPassantTargetSquare");

        final String halfmoveClock = matcher.group("halfmoveClock");
        this.halfmoveClock = halfmoveClock == null ? "0" : halfmoveClock;

        final String fullmoveClock = matcher.group("fullmoveClock");
        this.fullmoveClock = fullmoveClock == null ? "1" : fullmoveClock;
    }

    private boolean isValid() {
        final String[] rows = piecePlacement.split("/");

        final boolean piecePlacementRowsValid =
                Arrays.stream(rows)
                      .mapToInt(s -> s.chars().map(Fen::piecePlacementLength).sum())
                      .noneMatch(rowSum -> rowSum != 8);

        final boolean castlingAvailabilityValid = !castlingAvailability.isEmpty();

        return piecePlacementRowsValid && castlingAvailabilityValid;
    }

    public static Fen parse(final String input) {
        return new Fen(input.trim());
    }

    public static boolean isValid(final String input) {
        final Matcher matcher = FEN_PATTERN.matcher(input.trim());

        if (!matcher.matches()) {
            return false;
        }

        return new Fen(input, matcher).isValid();
    }

    private static int piecePlacementLength(final int i) {
        if (Character.isDigit(i)) {
            return i - '0';
        }

        return 1;
    }

    public String getInput() {
        return input;
    }

    public String getPiecePlacement() {
        return piecePlacement;
    }

    public String getActiveColor() {
        return activeColor;
    }

    public String getCastlingAvailability() {
        return castlingAvailability;
    }

    public String getEnPassantTargetSquare() {
        return enPassantTargetSquare;
    }

    public String getHalfmoveClock() {
        return halfmoveClock;
    }

    public String getFullmoveClock() {
        return fullmoveClock;
    }

    @Override
    public String toString() {
        return input;
    }
}
