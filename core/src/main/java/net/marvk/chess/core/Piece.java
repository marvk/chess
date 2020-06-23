package net.marvk.chess.core;

import java.util.Objects;
import java.util.function.Function;

public enum Piece {
    /**
     This class uses Function instead of ColoredPiece because ColoredPiece cyclically depends on it.
     ColoredPiece values are null at initialization time.
     */

    KING(color -> coloredPiece(color, ColoredPiece.WHITE_KING, ColoredPiece.BLACK_KING)),
    QUEEN(color -> coloredPiece(color, ColoredPiece.WHITE_QUEEN, ColoredPiece.BLACK_QUEEN)),
    ROOK(color -> coloredPiece(color, ColoredPiece.WHITE_ROOK, ColoredPiece.BLACK_ROOK)),
    BISHOP(color -> coloredPiece(color, ColoredPiece.WHITE_BISHOP, ColoredPiece.BLACK_BISHOP)),
    KNIGHT(color -> coloredPiece(color, ColoredPiece.WHITE_KNIGHT, ColoredPiece.BLACK_KNIGHT)),
    PAWN(color -> coloredPiece(color, ColoredPiece.WHITE_PAWN, ColoredPiece.BLACK_PAWN));

    private final Function<Color, ColoredPiece> ofColor;

    Piece(final Function<Color, ColoredPiece> ofColor) {
        this.ofColor = ofColor;
    }

    public ColoredPiece ofColor(final Color color) {
        return ofColor.apply(color);
    }

    private static ColoredPiece coloredPiece(final Color color, final ColoredPiece whitePiece, final ColoredPiece blackPiece) {
        if (Objects.requireNonNull(color) == Color.WHITE) {
            return whitePiece;
        }

        return blackPiece;
    }
}
