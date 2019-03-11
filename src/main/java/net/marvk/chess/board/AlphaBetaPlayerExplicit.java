package net.marvk.chess.board;

import net.marvk.chess.util.Util;

import java.util.*;
import java.util.stream.Collectors;

public class AlphaBetaPlayerExplicit extends Player implements LastEvaluationGettable {
    private final Heuristic heuristic;
    private final int maxDepth;
    private Map<Move, Double> lastEvaluation;

    private Node lastRoot;

    public AlphaBetaPlayerExplicit(final Color color, final Heuristic heuristic, final int maxDepth) {
        super(color);
        this.heuristic = heuristic;
        this.maxDepth = maxDepth;
    }

    @Override
    public Move play(final MoveResult previousMove) {
        final Node root = new Node(null, previousMove);

        lastRoot = root;

        root.startExploration();

        final int max = root.children.stream().mapToInt(n -> n.value).max().orElseThrow(IllegalStateException::new);

        Collections.shuffle(root.children);

        final Node node = root.children.stream()
                                       .filter(n -> n.value == max)
                                       .findFirst()
                                       .orElseThrow(IllegalStateException::new);

        lastEvaluation = root.children.stream().collect(Collectors.toMap(
                n -> n.getMoveResult().getMove(),
                n -> ((double) n.value))
        );

        return node.getMoveResult().getMove();
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
