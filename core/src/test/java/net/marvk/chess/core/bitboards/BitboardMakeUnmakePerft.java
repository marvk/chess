package net.marvk.chess.core.bitboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.marvk.chess.core.board.Fen;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BitboardMakeUnmakePerft {
    private static final NominalPerft INITIAL_POSITION = new NominalPerft("initial position", Fen.STARTING_POSITION.getInput(),
            new NominalPerftStep(1, 20L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
            , new NominalPerftStep(2, 400L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
            , new NominalPerftStep(3, 8_902L, 34L, 0L, 0L, 0L, 12L, 0L, 0L, 0L)
            , new NominalPerftStep(4, 197_281L, 1_576L, 0L, 0L, 0L, 469L, 0L, 0L, 8L)
            , new NominalPerftStep(5, 4_865_609L, 82_719L, 258L, 0L, 0L, 27_351L, 6L, 0L, 347L)
            , new NominalPerftStep(6, 119_060_324L, 2_812_008L, 5_248L, 0L, 0L, 809_099L, 329L, 46L, 10_828L)
    );

    private static final NominalPerft POSITION_2 = new NominalPerft("position 2", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -",
            new NominalPerftStep(1, 48L)
            , new NominalPerftStep(2, 2_039L)
            , new NominalPerftStep(3, 97_862L)
            , new NominalPerftStep(4, 4_085_603L)
            , new NominalPerftStep(5, 193_690_690L)
    );

    private static final NominalPerft POSITION_3 = new NominalPerft("position 3", "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -",
            new NominalPerftStep(1, 14L)
            , new NominalPerftStep(2, 191L)
            , new NominalPerftStep(3, 2_812L)
            , new NominalPerftStep(4, 43_238L)
            , new NominalPerftStep(5, 674_624L)
            , new NominalPerftStep(6, 11_030_083L)
            , new NominalPerftStep(7, 178_633_661L)
    );

    private static final NominalPerft POSITION_4 = new NominalPerft("position 4", "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
            new NominalPerftStep(1, 6L)
            , new NominalPerftStep(2, 264L)
            , new NominalPerftStep(3, 9_467L)
            , new NominalPerftStep(4, 422_333L)
            , new NominalPerftStep(5, 15_833_292L)
            , new NominalPerftStep(6, 706_045_033L)
    );

    private static final NominalPerft POSITION_4_MIRRORED = new NominalPerft("position 4 mirrored", "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1",
            new NominalPerftStep(1, 6L)
            , new NominalPerftStep(2, 264L)
            , new NominalPerftStep(3, 9_467L)
            , new NominalPerftStep(4, 422_333L)
            , new NominalPerftStep(5, 15_833_292L)
            , new NominalPerftStep(6, 706_045_033L)
    );

    private static final NominalPerft POSITION_5 = new NominalPerft("position 5", "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ",
            new NominalPerftStep(1, 44L)
            , new NominalPerftStep(2, 1_486L)
            , new NominalPerftStep(3, 62_379L)
            , new NominalPerftStep(4, 2_103_487L)
            , new NominalPerftStep(5, 89_941_194L)
    );

    private static final NominalPerft POSITION_6 = new NominalPerft("position 6", "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
            new NominalPerftStep(1, 46L)
            , new NominalPerftStep(2, 2_079L)
            , new NominalPerftStep(3, 89_890L)
            , new NominalPerftStep(4, 3_894_594L)
            , new NominalPerftStep(5, 164_075_551L)
    );

    private static Path enginePath;

    private static List<NominalPerft> perfts() {
        final List<NominalPerft> perfts = new ArrayList<>();

        perfts.add(INITIAL_POSITION);
        perfts.add(POSITION_2);
        perfts.add(POSITION_3);
        perfts.add(POSITION_4);
        perfts.add(POSITION_4_MIRRORED);
        perfts.add(POSITION_5);
        perfts.add(POSITION_6);

        return perfts;
    }

    @BeforeAll
    public static void setup() {
        enginePath = Paths.get("B:/Marvin/Desktop/Programme/stockfish-10-win/Windows/stockfish_10_x64.exe");
    }

    @ParameterizedTest
    @MethodSource("perfts")
    public void perftTest(final NominalPerft nominalPerft) {
        for (int i = 1; i <= nominalPerft.depth; i++) {
            final NominalPerftStep step = nominalPerft.getForDepth(i);
            final long perft = perft(new Bitboard(nominalPerft.fen), step.depth);

            Assertions.assertEquals(step.nodes, perft, "Depth " + i + "\n" + nominalPerft.fen + "\n");
        }
    }

    private static long perft(final Bitboard board, final int depth) {
        if (depth == 0) {
            return 1L;
        }

        final List<Bitboard.BBMove> moves = board.getPseudoLegalMoves();

        long nodes = 0L;

        for (final Bitboard.BBMove move : moves) {
            board.make(move);

//            board.unmake(move);
//
//            Assertions.assertEquals(hash, board.hashCode(), () -> {
//                final StringJoiner error = new StringJoiner("\n");
//                final String actual = board.toString();
//
//                error.add("Wrong undo");
//                error.add(fen);
//                error.add("depth: " + depth);
//                error.add("Previous\t" + previous);
//                error.add("Move \t\t" + move.uci());
//                error.add("expected:");
//                error.add(expected);
//                error.add("actual:");
//                error.add(actual);
//                error.add(Boolean.toString(expected.equals(actual)));
//                error.add(black);
//                error.add(board.blackString());
//
//                return error.toString();
//            });
//
//            board.make(move);

            final boolean valid = !board.isInvalidPosition();

            if (valid) {
                nodes += perft(board, depth - 1);
            }

            board.unmake(move);
        }

        return nodes;
    }

    private static class NominalPerft {
        private final String name;
        private final Fen fen;
        private final Map<Integer, NominalPerftStep> nominal;
        private final int depth;

        NominalPerft(final String name, final String fen, final NominalPerftStep... nominalPerftSteps) {
            this.name = name;

            this.fen = Fen.parse(fen);

            this.nominal = Arrays.stream(nominalPerftSteps)
                                 .collect(Collectors.toMap(NominalPerftStep::getDepth, Function.identity()));

            this.depth = Arrays.stream(nominalPerftSteps)
                               .mapToInt(NominalPerftStep::getDepth)
                               .max()
                               .orElseThrow(IllegalArgumentException::new);
        }

        NominalPerftStep getForDepth(final int depth) {
            return nominal.get(depth);
        }

        @Override
        public String toString() {
            return name + ": " + fen;
        }
    }

    @AllArgsConstructor
    @Data
    private static class NominalPerftStep {
        private final int depth;
        private final long nodes;
        private final long captures;
        private final long enPassant;
        private final long castles;
        private final long promotions;
        private final long checks;
        private final long discoveryChecks;
        private final long doubleChecks;
        private final long checkmates;

        NominalPerftStep(final int depth, final long nodes) {
            this(depth, nodes, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L);
        }
    }
}
