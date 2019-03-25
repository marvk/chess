package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.Fen;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ZobristHashingTest {

    private int totalNodes;
    private int totalTableHits;
    private int totalCollisions;

    private int nodes;
    private int tableHits;
    private int collisions;
    private Map<Long, Bitboard> transpositionTable;

    private static List<Fen> fens() {
        return Stream.of(
                Fen.STARTING_POSITION.getInput(),
                "8/r2p4/3N3p/n4P1q/3P2k1/P2PQ1p1/pK6/2R5 w - - 0 1",
                "8/1N1p1pPK/4R3/2nP4/1ppB4/1b1B1k2/1Q4n1/8 w - - 0 1",
                "7n/5Nk1/p7/p1N5/3pP3/1p2Q1K1/1P3PPB/3r4 w - - 0 1",
                "8/6K1/1B2PP2/3b3p/2p3nk/3PQ1p1/p1n2P2/1R6 w - - 0 1",
                "2k5/6p1/3P1PP1/1R1K4/5p1P/n4Rp1/1n3bB1/7b w - - 0 1",
                "8/2R2N2/5ppp/P2b4/1B1q1kP1/6pr/3r3R/2K5 w - - 0 1",
                "B1r5/1KB3p1/2n4p/1k4b1/nP6/p5Rb/2Q3p1/8 w - - 0 1",
                "5n2/3k4/qr4PQ/3B1K1R/3pp2p/p5P1/P2B4/8 w - - 0 1",
                "R7/2p1ppP1/P7/b2k4/2RN4/1p3P1B/2p4K/5Q2 w - - 0 1",
                "7K/1RP5/5P1p/2k2P2/P1p5/r3rp2/p1bbp3/8 w - - 0 1",
                "8/5r2/4K3/4N3/3k4/8/8/8 w - - 0 1",
                "8/2k5/8/b7/8/1K6/4P3/8 w - - 0 1",
                "7k/5P2/8/8/8/8/4p3/2K5 w - - 0 1",
                "8/8/8/3B4/1P6/8/6K1/4k3 w - - 0 1",
                "K7/8/5p2/3k4/8/8/8/4r3 w - - 0 1",
                "8/4P3/8/3K4/8/7p/2k5/8 w - - 0 1",
                "8/8/8/4P1k1/7n/4K3/8/8 w - - 0 1",
                "8/8/8/1k6/7P/8/K5p1/8 w - - 0 1",
                "4K3/8/2r5/8/4p3/8/8/3k4 w - - 0 1",
                "8/5Bkp/8/7K/8/8/8/8 w - - 0 1",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
                "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
                "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
                "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1",
                "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
                "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10"
        ).map(Fen::parse).collect(Collectors.toList());
    }

    @BeforeAll
    public void setup() {
        transpositionTable = new HashMap<>();
    }

    @BeforeEach
    public void reset() {
        totalNodes += nodes;
        totalTableHits += tableHits;
        totalCollisions += collisions;

        nodes = 0;
        tableHits = 0;
        collisions = 0;
    }

    @AfterAll
    public void printResult() {
        System.out.println("Total results");
        print(totalNodes, totalTableHits, totalCollisions);
    }

    private static void print(final int nodes, final int tableHits, final int collisions) {
        final double collisionRate = ((double) collisions) / tableHits;
        System.out.println("nodes\t\t\t" + nodes);
        System.out.println("table hits\t\t" + tableHits);
        System.out.println("collisions\t\t" + collisions);
        System.out.println("collision rate\t" + collisionRate);
        System.out.println();
    }

    @ParameterizedTest
    @MethodSource("fens")
    public void testCollisions(final Fen fen) {
        final Bitboard board = new Bitboard(fen);

        transpositionTable.put(board.zobristHash(), new Bitboard(board));

        deepen(board, 4);

        System.out.println("Results for " + fen + ":");
        print(nodes, tableHits, collisions);
    }

    public void deepen(final Bitboard board, final int depth) {
        nodes++;

        final long zobristHash = board.zobristHash();
        final Bitboard value = new Bitboard(board);
        final Bitboard hit = transpositionTable.get(zobristHash);

        if (hit == null) {
            transpositionTable.put(zobristHash, value);
        } else {
            tableHits++;

            if (!value.equalsZobrist(hit)) {
                System.out.println(value);
                System.out.println(hit);
                Assertions.fail();
            }
        }

        if (depth == 0) {
            return;
        }

        final List<Bitboard.BBMove> bbMoves = board.generatePseudoLegalMoves();

        for (final Bitboard.BBMove current : bbMoves) {
            board.make(current);

            if (board.isInvalidPosition()) {
                board.unmake(current);
                continue;
            }

            deepen(board, depth - 1);

            board.unmake(current);
        }
    }
}