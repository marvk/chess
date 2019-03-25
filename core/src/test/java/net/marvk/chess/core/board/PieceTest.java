package net.marvk.chess.core.board;

import net.marvk.chess.core.Color;
import net.marvk.chess.core.Piece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PieceTest {
    @Test
    public void testAssociation() {
        for (final Color color : Color.values()) {
            for (final Piece piece : Piece.values()) {
                assertEquals(piece, piece.ofColor(color).getPiece());
                assertEquals(color, piece.ofColor(color).getColor());
            }
        }
    }
}