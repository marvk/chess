package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Fen;
import net.marvk.chess.core.board.Move;
import net.marvk.chess.core.board.MoveResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

class BitboardPerft {
    private Path enginePath;

    @BeforeAll
    public void setup() {
        this.enginePath = Paths.get("B:/Marvin/Desktop/Programme/perft-10-win/Windows/stockfish_10_x64.exe");
    }

    @Test
    public void position1() {
        final Bitboard bitboard = new Bitboard(Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));

        DeepenResult result = new DeepenResult();

        result.validMoves = List.of(new MoveResult(bitboard, Move.NULL_MOVE));

        result = testAndDeepen(result, 20);
        result = testAndDeepen(result, 400);
        result = testAndDeepen(result, 8_902);
        result = testAndDeepen(result, 197_281);
        result = testAndDeepen(result, 4_865_609);
//        result = testAndDeepen(result, 119_060_324);
    }

    @Test
    public void position2() {
        final Bitboard bitboard = new Bitboard(Fen.parse("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -"));

        DeepenResult result = new DeepenResult();

        result.validMoves = List.of(new MoveResult(bitboard, Move.NULL_MOVE));

        result = testAndDeepen(result, 48);
        result = testAndDeepen(result, 2_039);
        result = testAndDeepen(result, 97_862);
        result = testAndDeepen(result, 4_085_603);
//        result = testAndDeepen(result, 193_690_690);
    }

    @Test
    public void position3() {
        final Bitboard bitboard = new Bitboard(Fen.parse("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -"));

        DeepenResult result = new DeepenResult();

        result.validMoves = List.of(new MoveResult(bitboard, Move.NULL_MOVE));

        result = testAndDeepen(result, 14);
        result = testAndDeepen(result, 191);
        result = testAndDeepen(result, 2_812);
        result = testAndDeepen(result, 43_238);
        result = testAndDeepen(result, 674_624);
        result = testAndDeepen(result, 11_030_083);
    }

    @Test
    public void position4() {
        final Bitboard bitboard = new Bitboard(Fen.parse("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"));

        DeepenResult result = new DeepenResult();

        result.validMoves = List.of(new MoveResult(bitboard, Move.NULL_MOVE));

        result = testAndDeepen(result, 6);
        result = testAndDeepen(result, 264);
        result = testAndDeepen(result, 9_467);
        result = testAndDeepen(result, 422_333);
        result = testAndDeepen(result, 15_833_292);
    }

    @Test
    public void position4Mirrored() {
        final Bitboard bitboard = new Bitboard(Fen.parse("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1"));

        DeepenResult result = new DeepenResult();

        result.validMoves = List.of(new MoveResult(bitboard, Move.NULL_MOVE));

        result = testAndDeepen(result, 6);
        result = testAndDeepen(result, 264);
        result = testAndDeepen(result, 9_467);
        result = testAndDeepen(result, 422_333);
        result = testAndDeepen(result, 15_833_292);
    }

    @Test
    public void position5() {
        final Bitboard bitboard = new Bitboard(Fen.parse("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"));

        DeepenResult result = new DeepenResult();

        result.validMoves = List.of(new MoveResult(bitboard, Move.NULL_MOVE));

        result = testAndDeepen(result, 44);
        result = testAndDeepen(result, 1_486);
        result = testAndDeepen(result, 62_379);
        result = testAndDeepen(result, 2_103_487);
//        result = testAndDeepen(result, 89_941_194);
    }

    @Test
    public void position6() {
        final Bitboard bitboard = new Bitboard(Fen.parse("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10"));

        DeepenResult result = new DeepenResult();

        result.validMoves = List.of(new MoveResult(bitboard, Move.NULL_MOVE));

        result = testAndDeepen(result, 46);
        result = testAndDeepen(result, 2_079);
        result = testAndDeepen(result, 89_890);
        result = testAndDeepen(result, 3_894_594);
//        result = testAndDeepen(result, 164_075_551);
    }

    private DeepenResult testAndDeepen(DeepenResult previous, final int validMoves) {
//        if (validMoves == 264) {
//            stockfishDiff(previous);
//        }

        final DeepenResult next = deepen(previous);

        Assertions.assertEquals(validMoves, next.validMoves.size());

        return next;
    }

    private static DeepenResult deepen(final DeepenResult previous) {
        final List<MoveResult> collect =
                previous.validMoves
                        .stream()
                        .parallel()
                        .map(MoveResult::getBoard)
                        .map(Bitboard::getValidMoves)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        final DeepenResult deepenResult = new DeepenResult();

        deepenResult.validMoves = collect;

        return deepenResult;
    }

    private void stockfishDiff(final DeepenResult previous) {
        previous.validMoves.parallelStream().forEach(validMove -> {
            final Set<String> generatedMoves =
                    validMove.getBoard()
                             .getValidMoves()
                             .stream()
                             .map(MoveResult::getMove)
                             .map(Move::getUci)
                             .collect(Collectors.toSet()
                             );

            final String fen = validMove.getBoard().fen();

            final Set<String> stockfishMoves;
            try {
                stockfishMoves = new EnginePerft(enginePath).perft(fen);
            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
                return;
            }

            final Set<String> wrongMoves = new HashSet<>(generatedMoves);
            wrongMoves.removeAll(stockfishMoves);

            stockfishMoves.removeAll(generatedMoves);

            if (!wrongMoves.isEmpty() || !stockfishMoves.isEmpty()) {
                System.out.println("fen = " + fen);
                System.out.println("wrongMoves = " + wrongMoves);
                System.out.println("missingMoves = " + stockfishMoves);
            }
        });
    }

    private static class DeepenResult {
        private List<MoveResult> validMoves;
    }
}