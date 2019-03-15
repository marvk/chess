package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.Direction;
import net.marvk.chess.core.board.Piece;
import net.marvk.chess.core.board.Square;

import java.util.*;

public class Configuration {
    private static final Random RANDOM = new Random(0);

    private final Piece piece;
    private final Square square;
    private final List<Direction> directions;
    private final List<Square> relevantSquares;
    private final long mask;
    private final long[] possibleConfigurations;
    private final int numPossibleConfigurations;
    private final int numRelevantSquares;
    private final int hashMask;
    private final long hashShift;
    private final long magic;

    private Configuration(final Piece piece, final Square square) {
        if (piece == Piece.ROOK) {
            directions = Direction.ORTHOGONAL_DIRECTIONS;
        } else if (piece == Piece.BISHOP) {
            directions = Direction.DIAGONAL_DIRECTIONS;
        } else {
            throw new IllegalArgumentException();
        }

        this.piece = piece;
        this.square = square;
        this.relevantSquares = relevantSquares(square, directions);
        this.numRelevantSquares = relevantSquares.size();

        this.mask = MagicBitboards.setAllBits(0L, relevantSquares);

        this.numPossibleConfigurations = (int) Math.pow(2, numRelevantSquares);
        this.possibleConfigurations = possibleConfigurations();

        this.hashMask = (1 << numRelevantSquares) - 1;
        this.hashShift = 64L - numRelevantSquares;

        this.magic = findMagic();
    }

    public long[] generateAllAttacks() {
        final long[] result = new long[numPossibleConfigurations];

        for (final long configuration : possibleConfigurations()) {
            result[hash(configuration)] = generateAttacksForConfiguration(configuration);
        }

        return result;
    }

    private long generateAttacksForConfiguration(final long configuration) {
        if (configuration == 144) {
            System.out.println();
        }

        long result = 0L;

        for (final Direction direction : directions) {
            Square current = square.translate(direction);

            while (current != null && (configuration & current.getOccupiedBitMask()) == 0L) {

                result = MagicBitboards.setBit(result, current);

                current = current.translate(direction);
            }
        }

        return result;
    }

    private long[] possibleConfigurations() {
        final long[] result = new long[numPossibleConfigurations];

        for (int i = 0; i < numPossibleConfigurations; i++) {
            final String s = MagicBitboards.toPaddedBinaryString(i, numRelevantSquares);

            long current = 0L;

            for (int j = 0; j < numRelevantSquares; j++) {
                if (s.charAt(j) == '1') {
                    current = MagicBitboards.setBit(current, relevantSquares.get(j));
                }
            }

            result[i] = current;
        }

        return result;
    }

    public Piece getPiece() {
        return piece;
    }

    public Square getSquare() {
        return square;
    }

    private long findMagic() {
        while (true) {
            final long candidate = magicCandidate();

            if (checkCandidate(candidate)) {
                return candidate;
            }
        }
    }

    private boolean checkCandidate(final long candidate) {
        final Set<Object> hashes = new HashSet<>(numPossibleConfigurations);

        for (final long possibleConfiguration : possibleConfigurations) {
            final long result = ((possibleConfiguration * candidate) >> hashShift) & hashMask;

            if (!hashes.add(result)) {
                return false;
            }
        }

        return true;
    }

    private int hash(final long l) {
        return ((int) ((((l & mask) * magic) >> hashShift) & hashMask));
    }

    public long getMask() {
        return mask;
    }

    public int getHashMask() {
        return hashMask;
    }

    public long getHashShift() {
        return hashShift;
    }

    public long getMagic() {
        return magic;
    }

    private static long magicCandidate() {
        return RANDOM.nextLong() & RANDOM.nextLong() & RANDOM.nextLong();
    }

    private static List<Square> relevantSquares(final Square square, final Collection<Direction> directions) {
        final List<Square> result = new ArrayList<>();

        for (final Direction direction : directions) {
            Square current = square.translate(direction);

            while (current != null && !current.isEdgeForDirection(direction)) {

                result.add(current);

                current = current.translate(direction);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public static Configuration rookConfiguration(final Square square) {
        return new Configuration(Piece.ROOK, square);
    }

    public static Configuration bishopConfiguration(final Square square) {
        return new Configuration(Piece.BISHOP, square);
    }

    public List<Square> getRelevantSquares() {
        return relevantSquares;
    }

    public static void main(String[] args) {
        final Configuration x = Configuration.rookConfiguration(Square.A8);

        System.out.println(MagicBitboards.toBoardString(x.getMask()));

        final long l1 = MagicBitboards.setAllBits(0L, Arrays.asList(Square.A4, Square.C3));

        System.out.println(MagicBitboards.toBoardString(l1));

        final long l = x.generateAllAttacks()[x.hash(l1)];

        System.out.println(MagicBitboards.toBoardString(l));

    }
}
