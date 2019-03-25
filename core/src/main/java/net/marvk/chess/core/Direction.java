package net.marvk.chess.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Direction {
    NORTH(1, 0),
    NORTH_NORTH_EAST(2, 1),
    NORTH_EAST(1, 1),
    NORTH_EAST_EAST(1, 2),
    EAST(0, 1),
    EAST_SOUTH_EAST(-1, 2),
    SOUTH_EAST(-1, 1),
    SOUTH_SOUTH_EAST(-2, 1),
    SOUTH(-1, 0),
    SOUTH_SOUTH_WEST(-2, -1),
    SOUTH_WEST(-1, -1),
    WEST_SOUTH_WEST(-1, -2),
    WEST(0, -1),
    WEST_NORTH_WEST(1, -2),
    NORTH_WEST(1, -1),
    NORTH_NORTH_WEST(2, -1);

    private final int rankDifference;
    private final int fileDifference;

    private final Type type;

    public static final List<Direction> ORTHOGONAL_DIRECTIONS = groupByType(Type.ORTHOGONAL);
    public static final List<Direction> DIAGONAL_DIRECTIONS = groupByType(Type.DIAGONAL);
    public static final List<Direction> KNIGHT_DIRECTIONS = groupByType(Type.KNIGHT);
    public static final List<Direction> CARDINAL_DIRECTIONS =
            Stream.concat(ORTHOGONAL_DIRECTIONS.stream(), DIAGONAL_DIRECTIONS.stream())
                  .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

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
                     .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public enum Type {
        ORTHOGONAL,
        DIAGONAL,
        KNIGHT;
    }
}
