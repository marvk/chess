package net.marvk.chess.board;

public enum Color {
    BLACK("b"),
    WHITE("w");

    private final String fen;

    Color(final String fen) {
        this.fen = fen;
    }

    public String getFen() {
        return fen;
    }

    public static Color getColorFromFen(final String fen) {
        if ("b".equals(fen)) {
            return BLACK;
        }

        if ("w".equals(fen)) {
            return WHITE;
        }

        return null;
    }
}
