package net.marvk.chess.board;

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