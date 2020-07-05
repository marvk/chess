package net.marvk.chess.arena;

import lombok.SneakyThrows;
import net.marvk.chess.core.Fen;
import net.marvk.chess.core.UciMove;
import net.marvk.chess.core.bitboards.Bitboard;
import net.marvk.chess.kairukuengine.KairukuEngine;
import net.marvk.chess.uci4j.EngineFactory;
import net.marvk.chess.uci4j.SimpleUciEngine;
import net.marvk.chess.uci4j.UciEngine;
import net.marvk.chess.uci4j.UiChannel;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class Arena {
    private final EngineHolder engine1;
    private final EngineHolder engine2;

    public Arena(final EngineFactory engineFactory1, final EngineFactory engineFactory2) {
        this.engine1 = new EngineHolder(engineFactory1);
        this.engine2 = new EngineHolder(engineFactory2);
    }

    public void play() {
        final Bitboard bitboard = new Bitboard(Fen.STARTING_POSITION);

        while (true) {
            makeMove(engine1, bitboard);
            System.out.println(bitboard);
            makeMove(engine2, bitboard);
            System.out.println(bitboard);
        }
    }

    private static void makeMove(final EngineHolder engine, final Bitboard bitboard) {
        final UciMove engineMove = engine.getBestMove(bitboard.fen());

        final Optional<Bitboard.BBMove> bbMove =
                bitboard.generatePseudoLegalMoves()
                        .stream()
                        .filter(e -> e.asUciMove().equals(engineMove))
                        .findFirst();

        if (bbMove.isEmpty()) {
            throw new IllegalStateException();
        }

        bitboard.make(bbMove.get());
    }

    public static class EngineHolder {
        private static final UciMove[] MOVES = new UciMove[0];

        private final UciEngine engine;

        private CountDownLatch countDownLatch;
        private UciMove result;

        public EngineHolder(final EngineFactory engineFactory) {
            this.engine = engineFactory.create(move -> {
                result = move;
                countDownLatch.countDown();
            });
        }

        @SneakyThrows
        public synchronized UciMove getBestMove(final String fen) {
            countDownLatch = new CountDownLatch(1);
            engine.uciNewGame();
            engine.position(fen, MOVES);
            engine.go();
            countDownLatch.await();

            return getAndResetResult();
        }

        private UciMove getAndResetResult() {
            final UciMove result = this.result;

            this.result = null;
            this.countDownLatch = null;
            return result;
        }
    }

    public static void main(final String[] args) {
        final Arena arena = new Arena(kairukuFactory(), stockfishFactory(Path.of(args[0])));

        arena.play();
    }

    public static EngineFactory kairukuFactory() {
        return KairukuEngine::new;
    }

    public static EngineFactory stockfishFactory(final Path path) {
        return new EngineFactory() {
            @Override
            @SneakyThrows
            public SimpleUciEngine create(final UiChannel uiChannel) {
                final Process exec = Runtime.getRuntime().exec(path.toAbsolutePath().toString());

                return null;
            }
        };
    }
}
