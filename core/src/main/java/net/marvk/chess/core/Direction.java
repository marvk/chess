package net.marvk.chess.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Direction {
    NORTH(1, 0),
    EAST(0, 1),
    SOUTH(-1, 0),
    WEST(0, -1),

    NORTH_EAST(NORTH, EAST),
    SOUTH_EAST(SOUTH, EAST),
    SOUTH_WEST(SOUTH, WEST),
    NORTH_WEST(NORTH, WEST),

    NORTH_NORTH_EAST(NORTH, NORTH_EAST),
    EAST_NORTH_EAST(EAST, NORTH_EAST),
    EAST_SOUTH_EAST(EAST, SOUTH_EAST),
    SOUTH_SOUTH_EAST(SOUTH, SOUTH_EAST),
    SOUTH_SOUTH_WEST(SOUTH, SOUTH_WEST),
    WEST_SOUTH_WEST(WEST, SOUTH_WEST),
    WEST_NORTH_WEST(WEST, NORTH_WEST),
    NORTH_NORTH_WEST(NORTH, NORTH_WEST);

    private final int rankDifference;
    private final int fileDifference;

    private final Type type;

    public static final List<Direction> ORTHOGONAL_DIRECTIONS = groupByType(Type.ORTHOGONAL);
    public static final List<Direction> DIAGONAL_DIRECTIONS = groupByType(Type.DIAGONAL);
    public static final List<Direction> KNIGHT_DIRECTIONS = groupByType(Type.KNIGHT);
    public static final List<Direction> CARDINAL_DIRECTIONS =
            Stream.concat(ORTHOGONAL_DIRECTIONS.stream(), DIAGONAL_DIRECTIONS.stream())
                  .collect(Collectors.toUnmodifiableList());

    Direction(final Direction d1, final Direction d2) {
        this(d1.rankDifference + d2.rankDifference, d1.fileDifference + d2.fileDifference);
    }

    Direction(final int rankDifference, final int fileDifference) {
        this.rankDifference = rankDifference;
        this.fileDifference = fileDifference;
        final int sum = Math.abs(rankDifference) + Math.abs(fileDifference);

        if (sum == 1) {
            type = Type.ORTHOGONAL;
        } else if (sum == 2) {
            type = Type.DIAGONAL;
        } else if (sum == 3) {
            type = Type.KNIGHT;
        } else {
            throw new AssertionError();
        }
    }

    public int getRankDifference() {
        return rankDifference;
    }

    public int getFileDifference() {
        return fileDifference;
    }

    public Type getType() {
        return type;
    }

    private static List<Direction> groupByType(final Type type) {
        return Arrays.stream(values())
                     .filter(e -> e.type == type)
                     .collect(Collectors.toUnmodifiableList());
    }

    public enum Type {
        ORTHOGONAL,
        DIAGONAL,
        KNIGHT;
    }
}
