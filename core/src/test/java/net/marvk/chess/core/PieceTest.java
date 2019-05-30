package net.marvk.chess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PieceTest {
    @Test
    public void testAssociation() {
        for (final Piece piece : Piece.values()) {
            for (final Color color : Color.values()) {
                final ColoredPiece coloredPiece = piece.ofColor(color);

                assertEquals(piece, coloredPiece.getPiece(), "wrong piece on " + color + " " + piece);
                assertEquals(color, coloredPiece.getColor(), "wrong color on " + color + " " + piece);
            }

            assertThrows(NullPointerException.class, () -> piece.ofColor(null));
        }
    }
}