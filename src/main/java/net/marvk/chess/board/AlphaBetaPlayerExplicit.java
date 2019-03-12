package net.marvk.chess.board;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.util.Stopwatch;
import net.marvk.chess.util.Util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class AlphaBetaPlayerExplicit extends Player implements LastEvaluationGettable {
    private final Heuristic heuristic;
    private final int maxDepth;

    private Map<Move, Double> lastEvaluation;
    private Node lastRoot;

    private int lastCount;

    private int totalCount;
    private Duration totalDuration;
    private Duration lastDuration;
    private int lastNps;

    public AlphaBetaPlayerExplicit(final Color color, final Heuristic heuristic, final int maxDepth) {
        super(color);
        this.heuristic = heuristic;
        this.maxDepth = maxDepth;

        this.totalCount = 0;
        this.totalDuration = Duration.ZERO;
    }

    @Override
    public Move play(final MoveResult previousMove) {
        final Node root = new Node(previousMove);

        lastCount = 0;
        lastRoot = root;

        lastDuration = Stopwatch.time(root::startExploration);
        lastNps = Util.nodesPerSecond(lastDuration, lastCount);

        totalCount += lastCount;
        totalDuration = totalDuration.plus(lastDuration);

        final int averageNodesPerSecond = Util.nodesPerSecond(totalDuration, totalCount);

        log.info("average NPS for " + getColor() + ": " + averageNodesPerSecond);

        final int max = root.children.stream()
                                     .mapToInt(Node::getValue)
                                     .max()
                                     .orElseThrow(IllegalStateException::new);

        log.info(getColor() + " used " + lastCount + " nodes to calculated currentState in " + lastDuration + " (" + lastNps + " NPS), evaluate is " + max);

        lastEvaluation = root.children.stream().collect(Collectors.toMap(
                n -> n.getCurrentState().getMove(),
                n -> ((double) n.value))
        );

        return root.children.stream()
                            .filter(n -> n.value == max)
                            .findFirst()
                            .orElseThrow(IllegalStateException::new)
                            .getCurrentState()
                            .getMove();
    }

    @Override
    public Map<Move, Double> getLastEvaluation() {
        return lastEvaluation;
    }

    public Node getLastRoot() {
        return lastRoot;
    }

    public class Node {
        private final List<Node> children;
        private final Node parent;

        private int value;

        private final MoveResult currentState;

        public Node(final MoveResult currentState) {
            this(null, currentState);
        }

        Node(final Node parent, final MoveResult currentState) {
            this.parent = parent;
            lastCount++;

            this.currentState = currentState;
            this.children = new ArrayList<>();
        }

        private void startExploration() {
            value = explore(Integer.MIN_VALUE, Integer.MAX_VALUE, true, maxDepth);
        }

        private int explore(int alpha, int beta, final boolean maximise, final int depth) {
            if (depth == 0 || currentState.getBoard().findGameResult().isPresent()) {
                value = heuristic.evaluate(currentState.getBoard(), getColor());
                return value;
            }

            value = maximise ? Integer.MIN_VALUE : Integer.MAX_VALUE;

            final List<MoveResult> validMoves = currentState.getBoard().getValidMoves();

            Collections.shuffle(validMoves);

//            //Sort by piece difference to get better pruning
//            validMoves.sort(Comparator.comparing(moveResult -> {
//                final Board board = moveResult.getBoard();
//                return board.computeScore(Util.SCORES, getColor().opposite()) - board.computeScore(Util.SCORES, getColor());
//            }));

            for (final MoveResult current : validMoves) {
                final Node node = new Node(this, current);
                children.add(node);

                final int childValue = node.explore(alpha, beta, !maximise, depth - 1);

                if (maximise) {
                    value = Math.max(value, childValue);
                    alpha = Math.max(alpha, value);
                } else {
                    value = Math.min(value, childValue);
                    beta = Math.min(beta, value);
                }

                if (beta <= alpha) {
                    break;
                }
            }

            return value;
        }

        public MoveResult getCurrentState() {
            return currentState;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "children=" + children +
                    ", parent=" + parent +
                    ", value=" + value +
                    ", currentState=" + currentState +
                    '}';
        }

        public List<Node> getChildren() {
            return children;
        }

        public int getValue() {
            return value;
        }
    }

    public int getLastCount() {
        return lastCount;
    }

    public Duration getLastDuration() {
        return lastDuration;
    }

    public int getLastNps() {
        return lastNps;
    }
}
