package net.marvk.chess.core.board;

public enum Color {
    BLACK("b") {
        @Override
        public Color opposite() {
            return WHITE;
        }
    },
    WHITE("w") {
        @Override
        public Color opposite() {
            return BLACK;
        }
    };

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

    public abstract Color opposite();
}
