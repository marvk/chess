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

    private final MvvLvaPieceSquareDifferenceMoveOrder defaultMoveOrder = new MvvLvaPieceSquareDifferenceMoveOrder();
    private final MvvLvaMoveOrder quiescenceSearchMoveOrder = new MvvLvaMoveOrder();

    private final Heuristic heuristic = new SimpleHeuristic();

    private final ExecutorService executor;

    private Future<Void> calculationFuture;
    private int ply;
    private double plyBonus;

    private Bitboard board;

    private Color selfColor;

    private final Metrics metrics = new Metrics();
    private final TranspositionTable transpositionTable = new TranspositionTable(10_000_000);
    private final Set<Long> movesSinceHalfmoveReset = new HashSet<>();

    private final Set<UciMove> searchMoves = new HashSet<>();

    private Bitboard.BBMove[] previousPv;

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
        resetAll();
    }

    private void resetAll() {
        resetForMove();
        metrics.resetAll();
        board = null;
        plyBonus = 0.0;
        transpositionTable.clear();
        previousPv = null;

        // TODO remove last two (?) entries if no new position has been given since last go
        movesSinceHalfmoveReset.clear();
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

        selfColor = board.getActivePlayer();

        final Integer time = selfColor == Color.WHITE ? go.getWhiteTime() : go.getBlackTime();

        final int actualPly = calculatePly(go, time);

        log.info("time remaining is " + time + "ms, setting ply to " + ply + " (" + actualPly + " + " + ((int) plyBonus) + ")");

        calculationFuture = executor.submit(() -> {
            resetForMove();

            if (go.getSearchMoves() != null && go.getSearchMoves().length > 0) {
                searchMoves.addAll(Arrays.asList(go.getSearchMoves()));
            }

            final ValuedMove play;
            try {
                play = play();
            } catch (final Throwable t) {
                log.error("unexpected error, board state:\n" + board, t);
                throw new RuntimeException(t);
            }

            final List<ValuedMove> pv = Stream.iterate(play, vm -> vm.getPvChild() != null, ValuedMove::getPvChild)
                                              .collect(Collectors.toList());

            previousPv = Stream.iterate(play, vm -> vm.getPvChild() != null, ValuedMove::getPvChild)
                               .map(ValuedMove::getMove)
                               .toArray(Bitboard.BBMove[]::new);

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

    private int calculatePly(final Go go, final Integer timeRemaining) {
        if (go.getDepth() != null) {
            ply = go.getDepth();
            return ply;
        }

        if (timeRemaining == null) {
            ply = 7;
            return ply;
        }

        if (timeRemaining > 60_000) {
            ply = 6;
        } else if (timeRemaining > 20_000) {
            ply = 5;
        } else if (timeRemaining > 7_500) {
            ply = 4;
        } else if (timeRemaining > 2_000) {
            ply = 3;
        } else if (timeRemaining > 1_000) {
            ply = 2;
        } else {
            ply = 2;
        }

        if (board.getFullmoveClock() > 1 && metrics.getLastTableHitRate() < 0.75) {
            final long lastMillis = metrics.getLastDuration().toMillis();

            if (timeRemaining <= 30_000 && timeRemaining > 3_000) {
                if (lastMillis < 10L) {
                    plyBonus = 2;
                } else if (lastMillis < 100L) {
                    plyBonus = 1;
                } else {
                    plyBonus = 0.0;
                }
            } else {
                plyBonus = 0.0;
            }
        }

        final int actualPly = ply;

        ply += ((int) plyBonus);

        return actualPly;
    }

    private void resetForMove() {
        searchMoves.clear();
        metrics.resetRound();
    }

    @Override
    public void stop() {
        calculationFuture.cancel(true);
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
        movesSinceHalfmoveReset.add(board.zobristHash());

        final StopWatch stopwatch = StopWatch.createStarted();
        final ValuedMove result = negamax(ply, SimpleHeuristic.LOSS, SimpleHeuristic.WIN, selfColor);
        stopwatch.stop();

        final Duration duration = Duration.ofNanos(stopwatch.getNanoTime());

        metrics.incrementDuration(duration);

        log.info(infoString(result));

        final Bitboard.BBMove theMove = result.getMove();
        board.make(theMove);
        movesSinceHalfmoveReset.add(board.zobristHash());
        board.unmake(theMove);

        return result;
    }

    private ValuedMove negamax(final int depth, final int alphaOriginal, final int betaOriginal, final Color currentColor) {
        metrics.incrementNodeCount();

        final long zobristHash = board.zobristHash();

        if (depth < ply && movesSinceHalfmoveReset.contains(zobristHash)) {
            return new ValuedMove(SimpleHeuristic.DRAW, null, null);
        }

        final TranspositionTable.Entry ttEntry = transpositionTable.get(zobristHash);

        int alpha = alphaOriginal;
        int beta = betaOriginal;

        if (ttEntry != null) {
            if (ttEntry.getDepth() >= depth) {
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
        }

        final List<Bitboard.BBMove> pseudoLegalMoves = board.generatePseudoLegalMoves();

        if (depth == 0) {
            final boolean legalMovesRemaining = Bitboard.hasAnyLegalMoves(board, pseudoLegalMoves);

            if (legalMovesRemaining && Bitboard.hasAnyAttackMoves(pseudoLegalMoves)) {
                return quiescenceSearch(5, alpha, beta, currentColor);
            }

            final int value = currentColor.getHeuristicFactor() * heuristic.evaluate(board, legalMovesRemaining);

            return new ValuedMove(value, null, null);
        }

        if (previousPv != null) {
            final int i = 2 + ply - depth;

            if (i < previousPv.length) {
                // set the previous PV move as the first move to be searched
                defaultMoveOrder.sort(pseudoLegalMoves, previousPv[i]);
            } else {
                defaultMoveOrder.sort(pseudoLegalMoves);
            }
        } else {
            defaultMoveOrder.sort(pseudoLegalMoves);
        }

        int value = SimpleHeuristic.LOSS;
        ValuedMove bestChild = null;
        Bitboard.BBMove bestMove = null;

        boolean legalMovesEncountered = false;

        for (final Bitboard.BBMove current : pseudoLegalMoves) {
            if (depth == ply && !searchMoves.isEmpty() && !searchMoves.contains(current.asUciMove())) {
                continue;
            }

            board.make(current);

            if (board.isInvalidPosition()) {
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

        final ValuedMove result = new ValuedMove(value, bestMove, bestChild);

        //Don't store game ending moves to still get the quickest mate
        if (!SimpleHeuristic.isCheckmateValue(value)) {
            final TranspositionTable.NodeType type;

            if (value <= alphaOriginal) {
                type = TranspositionTable.NodeType.UPPERBOUND;
            } else if (value >= beta) {
                type = TranspositionTable.NodeType.LOWERBOUND;
            } else {
                type = TranspositionTable.NodeType.EXACT;
            }

            transpositionTable.put(zobristHash, new TranspositionTable.Entry(result, depth, value, type));
        }

        return result;
    }

    private ValuedMove quiescenceSearch(final int depth, final int initialAlpha, final int initialBeta, final Color currentColor) {
        final List<Bitboard.BBMove> pseudoLegalAttackMoves = board.generatePseudoLegalAttackMoves();

        // Pretent the game is not over for speed?!
        final int standingPat = currentColor.getHeuristicFactor() * heuristic.evaluate(board, true);

        if (standingPat >= initialBeta) {
            return new ValuedMove(initialBeta, null, null);
        }

        int alpha = initialAlpha < standingPat
                ? standingPat
                : initialAlpha;

        if (depth == 0) {
            return new ValuedMove(alpha, null, null);
        }

        quiescenceSearchMoveOrder.sort(pseudoLegalAttackMoves);

        Bitboard.BBMove bestMove = null;
        ValuedMove bestChild = null;

        for (final Bitboard.BBMove current : pseudoLegalAttackMoves) {
            board.make(current);

            if (board.isInvalidPosition()) {
                board.unmake(current);
                continue;
            }

            final ValuedMove child = quiescenceSearch(depth - 1, -initialBeta, -alpha, currentColor.opposite());
            final int value = -child.getValue();

            metrics.incrementNodeCount();

            board.unmake(current);

            if (value >= initialBeta) {
                return new ValuedMove(initialBeta, current, child);
            }

            if (value > alpha) {
                alpha = value;

                bestMove = current;
                bestChild = child;
            }
        }

        return new ValuedMove(alpha, bestMove, bestChild);
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
        lineJoiner.add("╠═══════════════════════════════════╣");
        addToJoiner(lineJoiner, "ttable hits", metrics.getLastTableHits());
        addToJoiner(lineJoiner, "ttable hit rate", ((int) (metrics.getLastTableHitRate() * 1000000.0)) / 1000000.0);
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
