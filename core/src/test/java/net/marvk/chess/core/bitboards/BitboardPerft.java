package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Board;
import net.marvk.chess.core.board.Fen;
import net.marvk.chess.core.board.MoveResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

class BitboardPerft {
    @Test
    public void position1() {
        final Bitboard bitboard = new Bitboard(Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));

        DeepenResult result = new DeepenResult();

        result.validMoves = bitboard.getValidMoves();
        Assertions.assertEquals(0, result.captures);
        Assertions.assertEquals(0, result.enPassants);
        Assertions.assertEquals(20, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(0, result.captures);
        Assertions.assertEquals(0, result.enPassants);
        Assertions.assertEquals(400, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(34, result.captures);
        Assertions.assertEquals(0, result.enPassants);
        Assertions.assertEquals(8_902, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(1_576, result.captures);
        Assertions.assertEquals(0, result.enPassants);
        Assertions.assertEquals(197_281, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(82_719, result.captures);
        Assertions.assertEquals(258, result.enPassants);
        Assertions.assertEquals(4_865_609, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(2_812_008, result.captures);
        Assertions.assertEquals(5_248, result.enPassants);
        Assertions.assertEquals(119_060_324, result.validMoves.size());
    }

    @Test
    public void position2() {
        final Board bitboard = new Bitboard(Fen.parse("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -"));

        DeepenResult result = new DeepenResult();

        result.validMoves = bitboard.getValidMoves();
        Assertions.assertEquals(48, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(2039, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(97_862, result.validMoves.size());

        result = deepen(result);
        Assertions.assertEquals(4_085_603, result.validMoves.size());
    }

    private static DeepenResult deepen(final DeepenResult previous) {
        final AtomicLong numCaptures = new AtomicLong(0L);
        final AtomicLong numEnPassants = new AtomicLong(0L);

        final List<MoveResult> collect =
                previous.validMoves
                        .stream()
                        .parallel()
                        .map(e -> {
                            final List<MoveResult> validMoves = e.getBoard().getValidMoves();

                            final long captures =
                                    validMoves.stream()
                                              .filter(m -> ((Bitboard) m.getBoard()).numPieces() < ((Bitboard) e
                                                      .getBoard()).numPieces())
                                              .count();

                            final long enPassants =
                                    validMoves.stream()
                                              .filter(m -> ((Bitboard) m.getBoard()).enPassantMove())
                                              .count();

                            numCaptures.addAndGet(captures);

                            numEnPassants.addAndGet(enPassants);

                            return validMoves;
                        })
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        final DeepenResult deepenResult = new DeepenResult();

        deepenResult.validMoves = collect;
        deepenResult.captures = numCaptures.get();
        deepenResult.enPassants = numEnPassants.get();

        return deepenResult;
    }

    private static class DeepenResult {
        private List<MoveResult> validMoves;
        private long captures;
        private long enPassants;
        private long castles;
        private long promotions;
        private long checks;
        private long discoveryChecks;
        private long doubleChecks;
        private long checkmates;
    }
}