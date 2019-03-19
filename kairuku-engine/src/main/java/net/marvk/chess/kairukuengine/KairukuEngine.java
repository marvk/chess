package net.marvk.chess.kairukuengine;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.bitboards.Bitboard;
import net.marvk.chess.core.board.*;
import net.marvk.chess.uci4j.*;
import org.apache.commons.lang3.time.StopWatch;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class KairukuEngine extends UciEngine {
    private static final String PLY_OPTION = "ply";
    private static final Comparator<Bitboard.BBMove> MOVE_ORDER_COMPARATOR = Comparator.comparing(Bitboard.BBMove::getPieceAttackedValue)
                                                                                       .reversed();

    private final ExecutorService executor;

    private Future<Void> calculationFuture;
    private int ply;
    private Bitboard board;

    private Color selfColor;

    private final Heuristic heuristic = new SimpleHeuristic();
    private final Metrics metrics = new Metrics();
    private final TranspositionTable transpositionTable = new TranspositionTable();

    public KairukuEngine(final UIChannel uiChannel) {
        super(uiChannel);

        this.ply = 7;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // region UCI Methods

    @Override
    public void uci() {
        uiChannel.idName("kairuku");
        uiChannel.optionSpin(PLY_OPTION, ply, 1, 7);
    }

    @Override
    public void setDebug(final boolean debug) {

    }

    @Override
    public void isReady() {
        uiChannel.readyOk();
    }

    @Override
    public void setOption(final String name, final String value) {
        if (PLY_OPTION.equals(name)) {
            ply = Integer.parseInt(value);
        }
    }

    @Override
    public void registerLater() {

    }

    @Override
    public void register(final String name, final String code) {

    }

    @Override
    public void uciNewGame() {
        stop();

        resetAll();

        board = null;
    }

    private void resetAll() {
        metrics.resetAll();
        transpositionTable.clear();
    }

    @Override
    public void positionFromDefault(final UciMove[] moves) {
        board = UciMove.getBoard(moves);
    }

    @Override
    public void position(final String fenString, final UciMove[] moves) {
        board = UciMove.getBoard(moves, Fen.parse(fenString));
    }

    @Override
    public void go(final Go go) {
        if (board == null) {
            log.warn("not going, no position loaded");
            return;
        }

        metrics.resetRound();

        selfColor = board.getActivePlayer();

        final Integer time = selfColor == Color.WHITE ? go.getWhiteTime() : go.getBlackTime();

        if (go.getDepth() != null) {
            ply = go.getDepth();
        } else if (time == null) {
            ply = 7;
        } else if (time < 200) {
            ply = 3;
        } else if (time < 5_000) {
            ply = 4;
        } else if (time < 30_000) {
            ply = 5;
        } else if (time < 120_000) {
            ply = 6;
        } else {
            ply = 7;
        }

        log.info("time is " + time + ", setting " + "ply to " + ply);

        calculationFuture = executor.submit(() -> {
            final ValuedMove play;
            try {
                play = play();
            } catch (final Throwable t) {
                log.error("unexpected error", t);
                throw new RuntimeException();
            }

            final List<ValuedMove> pv = Stream.iterate(play, vm -> vm.getPvChild() != null, ValuedMove::getPvChild)
                                              .collect(Collectors.toList());

            final UciMove[] pvArray =
                    pv.stream().map(ValuedMove::getMove)
                      .filter(Objects::nonNull)
                      .map(Bitboard.BBMove::asUciMove)
                      .toArray(UciMove[]::new);

            uiChannel.bestMove(play.getMove().asUciMove());

            try {
                final Info info =
                        Info.builder()
                            .nps(((long) metrics.getLastNps()))
                            .score(new Score(play.getValue(), null, null))
                            .depth(ply)
                            .principalVariation(pvArray)
                            .nodes(((long) metrics.getLastNodeCount()))
                            .time(((int) metrics.getLastDuration().toMillis()))
                            .generate();

                uiChannel.info(info);
            } catch (final Throwable t) {
                log.error("unexpected error", t);
            }

            return null;
        });
    }

    @Override
    public void stop() {
        calculationFuture.cancel(true);

        metrics.resetRound();
    }

    @Override
    public void ponderHit() {

    }

    @Override
    public void quit() {
        calculationFuture.cancel(true);

        resetAll();
    }

    // endregion

    private ValuedMove play() {
        final StopWatch stopwatch = StopWatch.createStarted();
        final ValuedMove result = negamax(ply, SimpleHeuristic.LOSS, SimpleHeuristic.WIN, selfColor);
        stopwatch.stop();

        final Duration duration = Duration.ofNanos(stopwatch.getNanoTime());

        metrics.incrementDuration(duration);

        log.info(infoString(result));

        return result;
    }

    private ValuedMove negamax(final int depth, final int alphaOriginal, final int betaOriginal, final Color currentColor) {
        metrics.incrementNodeCount();

        int alpha = alphaOriginal;
        int beta = betaOriginal;

        final long zobristHash = board.zobristHash();

        final TranspositionTable.Entry ttEntry = transpositionTable.get(zobristHash);

        if (ttEntry != null && ttEntry.getDepth() >= depth) {
            metrics.incrementTableHits();

            switch (ttEntry.getNodeType()) {
                case LOWERBOUND:
                    alpha = Math.max(alpha, ttEntry.getValue());
                    break;
                case UPPERBOUND:
                    beta = Math.min(beta, ttEntry.getValue());
            }

            if (ttEntry.getNodeType() == TranspositionTable.NodeType.EXACT || alpha >= beta) {
                return ttEntry.getValuedMove();
            }
        }

        if (depth == 0 || board.getHalfmoveClock() == 50) {
            final int value = currentColor.getHeuristicFactor() * heuristic.evaluate(board, board.hasAnyLegalMoves());

            return new ValuedMove(value, null, null);
        }

        final List<Bitboard.BBMove> pseudoLegalMoves = board.getPseudoLegalMoves();

        Collections.shuffle(pseudoLegalMoves);
        pseudoLegalMoves.sort(MOVE_ORDER_COMPARATOR);

        int value = SimpleHeuristic.LOSS;
        ValuedMove bestChild = null;
        Bitboard.BBMove bestMove = null;

        boolean legalMovesEncountered = false;

        for (final Bitboard.BBMove current : pseudoLegalMoves) {
            board.make(current);

            if (board.invalidPosition()) {
                board.unmake(current);
                continue;
            }

            legalMovesEncountered = true;

            final ValuedMove child = negamax(depth - 1, -beta, -alpha, currentColor.opposite());

            final int childValue = -child.getValue();

            if (childValue > value) {
                value = childValue;
                bestMove = current;
                bestChild = child;
            }

            alpha = Math.max(alpha, value);

            board.unmake(current);

            if (alpha >= beta) {
                break;
            }
        }

        if (!legalMovesEncountered) {
            return new ValuedMove(currentColor.getHeuristicFactor() * heuristic.evaluate(board, false), null, null);
        }

        final TranspositionTable.NodeType type;

        if (value <= alphaOriginal) {
            type = TranspositionTable.NodeType.UPPERBOUND;
        } else if (value >= beta) {
            type = TranspositionTable.NodeType.LOWERBOUND;
        } else {
            type = TranspositionTable.NodeType.EXACT;
        }

        final ValuedMove valuedMove = new ValuedMove(value, bestMove, bestChild);

        transpositionTable.put(zobristHash, new TranspositionTable.Entry(valuedMove, depth, value, type));

        return valuedMove;
    }

    // region String generation

    private String infoString(final ValuedMove play) {
        final StringJoiner lineJoiner = new StringJoiner("\n");
        lineJoiner.add(board.toString());

        lineJoiner.add("╔═══════════════════════════════════╗");

        addToJoiner(lineJoiner, "color", selfColor);
        addToJoiner(lineJoiner, "best move", play.getMove().asUciMove());
        addToJoiner(lineJoiner, "duration", metrics.getLastDuration());
        lineJoiner.add("╠═══════════════════════════════════╣");
        addToJoiner(lineJoiner, "nodes searched", metrics.getLastNodeCount());
        addToJoiner(lineJoiner, "ttable hits", metrics.getLastTableHits());
        addToJoiner(lineJoiner, "table load factor", transpositionTable.load());
        lineJoiner.add("╠═══════════════════════════════════╣");
        addToJoiner(lineJoiner, "nps last", metrics.getLastNps());
        addToJoiner(lineJoiner, "nps avg", metrics.getTotalNps());
        lineJoiner.add("╠═══════════════════════════════════╣");
        addToJoiner(lineJoiner, "value (cp)", play.getValue());

        lineJoiner.add("╚═══════════════════════════════════╝");
        return "Evaluation result:\n" + lineJoiner.toString();
    }

    private static void addToJoiner(final StringJoiner lineJoiner, final String name, final String value) {
        lineJoiner.add("║ " + name + padLeft(value, 33 - name.length()) + " ║");
    }

    private static void addToJoiner(final StringJoiner lineJoiner, final String name, final Object value) {
        addToJoiner(lineJoiner, name, value.toString());
    }

    private static String padLeft(final String s, final int n) {
        return String.format("%" + n + "s", s);
    }

    // endregion
}
