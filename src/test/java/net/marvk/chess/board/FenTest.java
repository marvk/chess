package net.marvk.chess.board;

import static org.junit.jupiter.api.Assertions.*;

class FenTest {

    @org.junit.jupiter.api.Test
    void isValid() {
        assertTrue(Fen.isValid("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        assertTrue(Fen.isValid("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"));
        assertTrue(Fen.isValid("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2"));
        assertTrue(Fen.isValid("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertTrue(Fen.isValid("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertFalse(Fen.isValid("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b -  1 2"));
        assertFalse(Fen.isValid("rnbqbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertFalse(Fen.isValid("rnbqkbnr/pp1ppppp/8/2p4/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertFalse(Fen.isValid("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - 1 2"));
        assertFalse(Fen.isValid("rnbqkbnr/pp1ppppp/44/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
    }

    @org.junit.jupiter.api.Test
    void constructor() {
        assertDoesNotThrow(() -> new Fen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        assertDoesNotThrow(() -> new Fen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"));
        assertDoesNotThrow(() -> new Fen("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2"));
        assertDoesNotThrow(() -> new Fen("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertDoesNotThrow(() -> new Fen("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertThrows(IllegalArgumentException.class, () -> new Fen("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b -  1 2"));
        assertThrows(IllegalArgumentException.class, () -> new Fen("rnbqbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertThrows(IllegalArgumentException.class, () -> new Fen("rnbqkbnr/pp1ppppp/8/2p4/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
        assertThrows(IllegalArgumentException.class, () -> new Fen("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - 1 2"));
        assertThrows(IllegalArgumentException.class, () -> new Fen("rnbqkbnr/pp1ppppp/44/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"));
    }
}