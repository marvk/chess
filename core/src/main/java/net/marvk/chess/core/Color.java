package net.marvk.chess.core;

public enum Color {
    BLACK("b", -1) {
        @Override
        public Color opposite() {
            return WHITE;
        }
    },
    WHITE("w", 1) {
        @Override
        public Color opposite() {
            return BLACK;
        }
    };

    private final String fen;
    private final int heuristicFactor;

    Color(final String fen, final int heuristicFactor) {
        this.fen = fen;
        this.heuristicFactor = heuristicFactor;
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

    public int getHeuristicFactor() {
        return heuristicFactor;
    }

    public abstract Color opposite();
}
