package net.marvk.chess.board;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.util.Stopwatch;
import net.marvk.chess.util.Util;

import java.time.Duration;
import java.util.*;
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

    public AlphaBetaPlayerExplicit(final Color color, final Heuristic heuristic, final int maxDepth) {
        super(color);
        this.heuristic = heuristic;
        this.maxDepth = maxDepth;

        this.totalCount = 0;
        this.totalDuration = Duration.ZERO;
    }

    @Override
    public Move play(final MoveResult previousMove) {
        final Node root = new Node(null, previousMove);

        lastCount = 0;
        lastRoot = root;

        final Duration lastDuration = Stopwatch.time(root::startExploration);

        final int nodesPerSecond = Util.nodesPerSecond(lastDuration, lastCount);


        totalCount += lastCount;
        totalDuration = totalDuration.plus(lastDuration);

        final int averageNodesPerSecond = Util.nodesPerSecond(totalDuration, totalCount);

        log.info("average NPS for " + getColor() + ": " + averageNodesPerSecond);

        final int max = root.children.stream()
                                     .mapToInt(Node::getValue)
                                     .max()
                                     .orElseThrow(IllegalStateException::new);

        log.info(getColor() + " used " + lastCount + " nodes to calculated move in " + lastDuration + " (" + nodesPerSecond + " NPS), evaluate is " + max);

        Collections.shuffle(root.children);

        lastEvaluation = root.children.stream().collect(Collectors.toMap(
                n -> n.getMoveResult().getMove(),
                n -> ((double) n.value))
        );

        return root.children.stream()
                            .filter(n -> n.value == max)
                            .findFirst()
                            .orElseThrow(IllegalStateException::new).getMoveResult().getMove();
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

        private int value;

        private final Color color;
        private final int depth;

        private final MoveResult move;

        Node(final Node parent, final MoveResult move) {
            lastCount++;

            this.move = move;

            this.color = parent == null ? getColor().opposite() : parent.color.opposite();

            this.depth = parent == null ? maxDepth : parent.depth - 1;

            this.children = new ArrayList<>();
        }

        private void startExploration() {
            value = explore(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        private int explore(int alpha, int beta) {
            if (depth == 0 || move.getBoard().findGameResult().isPresent()) {
                value = heuristic.evaluate(move.getBoard(), getColor());
                return value;
            }

            final boolean maximise = color == getColor();

            value = maximise ? Integer.MIN_VALUE : Integer.MAX_VALUE;

            final List<MoveResult> validMoves = move.getBoard().getValidMoves();

            //Sort by piece difference to get better pruning
            validMoves.sort(Comparator.comparing(moveResult -> {
                final Board board = moveResult.getBoard();
                return board.computeScore(Util.SCORES, getColor().opposite()) - board.computeScore(Util.SCORES, getColor());
            }));

            for (final MoveResult current : validMoves) {
                final Node node = new Node(this, current);
                children.add(node);

                final int childValue = node.explore(alpha, beta);

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

        public MoveResult getMoveResult() {
            return move;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "children=" + children +
                    ", value=" + value +
                    ", color=" + color +
                    ", depth=" + depth +
                    ", move=" + move +
                    '}';
        }

        public List<Node> getChildren() {
            return children;
        }

        public int getValue() {
            return value;
        }
    }
}
