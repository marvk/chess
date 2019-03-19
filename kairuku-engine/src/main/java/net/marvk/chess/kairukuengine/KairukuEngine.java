package net.marvk.chess.kairukuengine;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.bitboards.Bitboard;
import net.marvk.chess.core.board.*;
import net.marvk.chess.core.util.Stopwatch;
import net.marvk.chess.core.util.Util;
import net.marvk.chess.uci4j.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
public class KairukuEngine extends UciEngine {
    private static final String PLY_OPTION = "ply";

    private final ExecutorService executor;

    private Future<Void> calculationFuture;
    private int ply;
    private Bitboard board;

    private Color color;
    private int lastCount;

    private final Heuristic heuristic = new SimpleHeuristic();

    public KairukuEngine(final UIChannel uiChannel) {
        super(uiChannel);

        this.ply = 7;
        this.executor = Executors.newSingleThreadExecutor();

        resetStats();
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

        resetStats();

        board = null;
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

        color = board.getActivePlayer();

        final Integer time = color == Color.WHITE ? go.getWhiteTime() : go.getBlackTime();

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
            final UciMove play;
            try {
                play = play();
            } catch (final Throwable t) {
                t.printStackTrace();
                log.error("unexpected error", t);
                throw new RuntimeException();
            }

            final Info info =
                    Info.builder()
                        .nps(((long) lastNps))
                        .score(new Score(lastRoot.getValue(), null, null))
                        .depth(ply)
                        .nodes(((long) lastCount))
                        .time(((int) lastDuration.toMillis()))
                        .generate();

            uiChannel.info(info);
            uiChannel.bestMove(play);

            return null;
        });
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
    }

    // endregion

    private void resetStats() {
        lastRoot = null;
        totalDuration = Duration.ZERO;
        totalCount = 0;
        lastDuration = null;
        lastNps = 0;
    }

    private Node lastRoot;

    private Duration totalDuration;
    private int totalCount;
    private Duration lastDuration;
    private int lastNps;

    private UciMove play() {
        final Node root = new Node();

        lastCount = 0;
        lastRoot = root;
        lastDuration = Stopwatch.time(root::startExploration);
        lastNps = Util.nodesPerSecond(lastDuration, lastCount);

        totalCount += lastCount;
        totalDuration = totalDuration.plus(lastDuration);

        final int averageNodesPerSecond = Util.nodesPerSecond(totalDuration, totalCount);
        final int max = root.children.stream()
                                     .mapToInt(Node::getValue)
                                     .max()
                                     .orElseThrow(IllegalStateException::new);

        final String value;

        if (max > Integer.MAX_VALUE - 100_000) {
            value = "WIN";
        } else if (max < Integer.MIN_VALUE + 100_000) {
            value = "LOSE";
        } else {
            value = Double.toString(max);
        }

        final UciMove result = root.children.stream()
                                            .filter(n -> n.value == max)
                                            .findFirst()
                                            .map(Node::getMove)
                                            .map(Bitboard.BBMove::asUciMove)
                                            .orElseThrow(IllegalStateException::new);

        log.info(infoString(board, averageNodesPerSecond, value, result));

        return result;
    }

    // region String generation

    private String infoString(final Bitboard previousBoard, final int averageNodesPerSecond, final String value, final UciMove result) {
        final StringJoiner lineJoiner = new StringJoiner("\n");
        lineJoiner.add(previousBoard.toString());

        lineJoiner.add("╔═══════════════════════════════════╗");

        addToJoiner(lineJoiner, "color", color.toString());
        addToJoiner(lineJoiner, "best move", result.toString());
        addToJoiner(lineJoiner, "nodes searched", Integer.toString(lastCount));
        addToJoiner(lineJoiner, "duration", lastDuration.toString());
        addToJoiner(lineJoiner, "nps last", Integer.toString(lastNps));
        addToJoiner(lineJoiner, "nps avg", Integer.toString(averageNodesPerSecond));
        addToJoiner(lineJoiner, "value (cp)", value);

        lineJoiner.add("╚═══════════════════════════════════╝");
        return "Evaluation result:\n" + lineJoiner.toString();
    }

    private static void addToJoiner(final StringJoiner lineJoiner, final String name, final String value) {
        lineJoiner.add("║ " + name + padLeft(value, 33 - name.length()) + " ║");
    }

    private static String padLeft(final String s, final int n) {
        return String.format("%" + n + "s", s);
    }

    // endregion

    public class Node {
        private final List<Node> children;
        private final Node parent;

        private int value;

        private final Bitboard.BBMove theMove;

        Node() {
            this(null, null);
        }

        Node(final Node parent, final Bitboard.BBMove move) {
            this.parent = parent;
            this.theMove = move;
            lastCount++;

            this.children = new ArrayList<>();
        }

        private void startExploration() {
            value = explore(Integer.MIN_VALUE, Integer.MAX_VALUE, true, ply);
        }

        private int explore(int alpha, int beta, final boolean maximise, final int depth) {
            if (depth == 0) {
                value = heuristic.evaluate(board, color, board.hasAnyLegalMoves());

                return value;
            }

            if (board.getHalfmoveClock() == 50) {
                value = 0;

                return value;
            }

            value = maximise ? Integer.MIN_VALUE : Integer.MAX_VALUE;

            final List<Bitboard.BBMove> validMoves = board.getPseudoLegalMoves();

            Collections.shuffle(validMoves);

//            Sort by piece difference to get better pruning
            validMoves.sort(Comparator.comparing(m -> {
                // TODO investigate if this is correct
                final int value = m.attackedPieceValue();
                return maximise ? value : -value;
            }));

            boolean anyLegalMoves = false;

            final int hash = board.hashCode();

            for (final Bitboard.BBMove move : validMoves) {
                final Node node = new Node(this, move);

                board.make(move);

                if (board.invalidPosition()) {
                    board.unmake(move);
                    continue;
                }

                anyLegalMoves = true;

                children.add(node);

                final int childValue = node.explore(alpha, beta, !maximise, depth - 1);

                if (maximise) {
                    value = Math.max(value, childValue);
                    alpha = Math.max(alpha, value);
                } else {
                    value = Math.min(value, childValue);
                    beta = Math.min(beta, value);
                }

                board.unmake(move);

                if (hash != board.hashCode()) {
                    throw new IllegalStateException();
                }

                if (beta <= alpha) {
                    break;
                }
            }

            if (!anyLegalMoves) {
                value = heuristic.evaluate(board, color, false);

                return value;
            }

            return value;
        }

        public int getValue() {
            return value;
        }

        public Bitboard.BBMove getMove() {
            return theMove;
        }
    }
}
