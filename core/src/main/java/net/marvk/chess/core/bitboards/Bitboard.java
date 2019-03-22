package net.marvk.chess.core.bitboards;

import lombok.ToString;
import net.marvk.chess.core.board.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.marvk.chess.core.bitboards.MoveConstants.*;

public class Bitboard {
    // region Constants
    //     _____ ____  _   _  _____ _______       _   _ _______ _____
    //    / ____/ __ \| \ | |/ ____|__   __|/\   | \ | |__   __/ ____|
    //   | |   | |  | |  \| | (___    | |  /  \  |  \| |  | | | (___
    //   | |   | |  | | . ` |\___ \   | | / /\ \ | . ` |  | |  \___ \
    //   | |___| |__| | |\  |____) |  | |/ ____ \| |\  |  | |  ____) |
    //    \_____\____/|_| \_|_____/   |_/_/    \_\_| \_|  |_| |_____/

    private static final Square[] SQUARES;
    private static final long[] KNIGHT_ATTACKS;
    private static final long[] KING_ATTACKS;

    private static final long[] WHITE_PAWN_ATTACKS;
    private static final long[] BLACK_PAWN_ATTACKS;

    private static final int KING_VALUE = 0;
    private static final int QUEEN_VALUE = 900;
    private static final int ROOK_VALUE = 500;
    private static final int BISHOP_VALUE = 330;
    private static final int KNIGHT_VALUE = 320;
    private static final int PAWN_VALUE = 100;

    static {
        SQUARES = new Square[64];

        for (final Square square : Square.values()) {
            SQUARES[square.getBitboardIndex()] = square;
        }

        KNIGHT_ATTACKS = new long[64];

        for (final Square square : SQUARES) {
            KNIGHT_ATTACKS[square.getBitboardIndex()] = staticAttacks(Direction.KNIGHT_DIRECTIONS, square);
        }

        KING_ATTACKS = new long[64];

        for (final Square square : SQUARES) {
            KING_ATTACKS[square.getBitboardIndex()] = staticAttacks(Direction.CARDINAL_DIRECTIONS, square);
        }

        WHITE_PAWN_ATTACKS = new long[64];

        for (final Square square : SQUARES) {
            WHITE_PAWN_ATTACKS[square.getBitboardIndex()] = staticAttacks(List.of(Direction.NORTH_WEST, Direction.NORTH_EAST), square);
        }

        BLACK_PAWN_ATTACKS = new long[64];

        for (final Square square : SQUARES) {
            BLACK_PAWN_ATTACKS[square.getBitboardIndex()] = staticAttacks(List.of(Direction.SOUTH_WEST, Direction.SOUTH_EAST), square);
        }
    }

    private static final int[] BLACK_KING_TABLE_LATE = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50,
    };

    private static final int[] BLACK_KING_TABLE_MID = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20,
    };

    private static final int[] BLACK_QUEEN_TABLE = {
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -5, 0, 5, 5, 5, 5, 0, -5,
            0, 0, 5, 5, 5, 5, 0, -5,
            -10, 5, 5, 5, 5, 5, 0, -10,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20,
    };

    private static final int[] BLACK_ROOK_TABLE = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            0, 0, 0, 5, 5, 0, 0, 0,
    };

    private static final int[] BLACK_BISHOP_TABLE = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -10, -10, -10, -10, -10, -20,
    };

    private static final int[] BLACK_KNIGHT_TABLE = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50,
    };

    private static final int[] BLACK_PAWN_TABLE = {
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 25, 25, 10, 5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -20, -20, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0,
    };

    private static final int[] WHITE_KING_TABLE_LATE;
    private static final int[] WHITE_KING_TABLE_MID;
    private static final int[] WHITE_QUEEN_TABLE;
    private static final int[] WHITE_ROOK_TABLE;
    private static final int[] WHITE_BISHOP_TABLE;
    private static final int[] WHITE_KNIGHT_TABLE;
    private static final int[] WHITE_PAWN_TABLE;

    static {
        WHITE_KING_TABLE_LATE = mirror(BLACK_KING_TABLE_LATE);
        WHITE_KING_TABLE_MID = mirror(BLACK_KING_TABLE_MID);
        WHITE_QUEEN_TABLE = mirror(BLACK_QUEEN_TABLE);
        WHITE_ROOK_TABLE = mirror(BLACK_ROOK_TABLE);
        WHITE_BISHOP_TABLE = mirror(BLACK_BISHOP_TABLE);
        WHITE_KNIGHT_TABLE = mirror(BLACK_KNIGHT_TABLE);
        WHITE_PAWN_TABLE = mirror(BLACK_PAWN_TABLE);
    }

    // color -> piece -> mid / late
    private static final int[][][][] PIECE_SQUARE_VALUES = {
            {
                    {null, null},
                    {WHITE_PAWN_TABLE, WHITE_PAWN_TABLE},
                    {WHITE_KNIGHT_TABLE, WHITE_KNIGHT_TABLE},
                    {WHITE_BISHOP_TABLE, WHITE_BISHOP_TABLE},
                    {WHITE_ROOK_TABLE, WHITE_ROOK_TABLE},
                    {WHITE_QUEEN_TABLE, WHITE_QUEEN_TABLE},
                    {WHITE_KING_TABLE_MID, WHITE_KING_TABLE_LATE},
            },
            {
                    {null, null},
                    {BLACK_PAWN_TABLE, BLACK_PAWN_TABLE},
                    {BLACK_KNIGHT_TABLE, BLACK_KNIGHT_TABLE},
                    {BLACK_BISHOP_TABLE, BLACK_BISHOP_TABLE},
                    {BLACK_ROOK_TABLE, BLACK_ROOK_TABLE},
                    {BLACK_QUEEN_TABLE, BLACK_QUEEN_TABLE},
                    {BLACK_KING_TABLE_MID, BLACK_KING_TABLE_LATE},
            }
    };

    private static int[] mirror(final int[] inputArray) {
        return IntStream.range(0, 8)
                        .flatMap(i -> IntStream.range(0, 8).map(j -> inputArray[8 * (8 - i - 1) + j]))
                        .map(i -> -i)
                        .toArray();
    }

    private static final long WHITE_QUEEN_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.B1, Square.C1, Square.D1);
    private static final long WHITE_KING_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.F1, Square.G1);
    private static final long BLACK_QUEEN_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.B8, Square.C8, Square.D8);
    private static final long BLACK_KING_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.F8, Square.G8);

    private static final long RANK_ONE_SQUARES = getRankSquares(Rank.RANK_1);
    private static final long RANK_TWO_SQUARES = getRankSquares(Rank.RANK_2);

    private static final long RANK_SEVEN_SQUARES = getRankSquares(Rank.RANK_7);
    private static final long RANK_EIGHT_SQUARES = getRankSquares(Rank.RANK_8);

    private static long bitwiseOr(final Square... squares) {
        return Arrays.stream(squares).mapToLong(Square::getOccupiedBitMask).reduce(0L, (l1, l2) -> l1 | l2);
    }

    private static long getRankSquares(final Rank rank) {
        return Arrays.stream(SQUARES)
                     .filter(s -> s.getRank() == rank)
                     .mapToLong(Square::getOccupiedBitMask)
                     .reduce(0L, (l1, l2) -> l1 | l2);
    }

    private static long staticAttacks(final Collection<Direction> directions, final Square square) {
        return directions.stream()
                         .map(square::translate)
                         .filter(Objects::nonNull)
                         .mapToLong(Square::getOccupiedBitMask)
                         .reduce(0L, (l1, l2) -> l1 | l2);
    }

    // endregion

    // region Initialization
    //    _____ _   _ _____ _______ _____          _      _____ ______      _______ _____ ____  _   _
    //   |_   _| \ | |_   _|__   __|_   _|   /\   | |    |_   _|___  /   /\|__   __|_   _/ __ \| \ | |
    //     | | |  \| | | |    | |    | |    /  \  | |      | |    / /   /  \  | |    | || |  | |  \| |
    //     | | | . ` | | |    | |    | |   / /\ \ | |      | |   / /   / /\ \ | |    | || |  | | . ` |
    //    _| |_| |\  |_| |_   | |   _| |_ / ____ \| |____ _| |_ / /__ / ____ \| |   _| || |__| | |\  |
    //   |_____|_| \_|_____|  |_|  |_____/_/    \_\______|_____/_____/_/    \_\_|  |_____\____/|_| \_|

    private final PlayerBoard black;
    private final PlayerBoard white;

    private Color turn;
    private long enPassant = 0L;

    private int fullmoveClock;
    private int halfmoveClock;

//    private long zobristHash;

    /**
     * Copy constructor
     *
     * @param previous the board to be copied
     */
    private Bitboard(final Bitboard previous) {
        this.white = new PlayerBoard(previous.white);
        this.black = new PlayerBoard(previous.black);

        this.turn = previous.turn;
        this.enPassant = previous.enPassant;

        this.fullmoveClock = previous.fullmoveClock;
        this.halfmoveClock = previous.halfmoveClock;

//        this.zobristHash = zobristHash();
    }

    public Bitboard(final Fen fen) {
        this.white = new PlayerBoard();
        this.black = new PlayerBoard();

        turn = Color.getColorFromFen(fen.getActiveColor());
        halfmoveClock = Integer.parseInt(fen.getHalfmoveClock());
        fullmoveClock = Integer.parseInt(fen.getFullmoveClock());

        fen.getCastlingAvailability().chars().forEach(e -> {
            if (e == 'K') {
                white.kingSideCastle = true;
            } else if (e == 'Q') {
                white.queenSideCastle = true;
            } else if (e == 'k') {
                black.kingSideCastle = true;
            } else if (e == 'q') {
                black.queenSideCastle = true;
            }
        });

        final Square squareFromFen = Square.getSquareFromFen(fen.getEnPassantTargetSquare());

        if (squareFromFen != null) {
            enPassant = squareFromFen.getOccupiedBitMask();
        }

        loadFen(fen);

//        this.zobristHash = zobristHash();
    }

    private void loadFen(final Fen fen) {
        final String[] split = fen.getPiecePlacement().split("/");

        for (int i = 0; i < split.length; i++) {
            final String line = split[8 - i - 1];

            for (int j = 0, lineIndex = 0; j < line.length(); j++) {
                final char c = line.charAt(j);

                if (Character.isDigit(c)) {
                    lineIndex += c - '0';
                    continue;
                }

                final int index = i * 8 + lineIndex;

                final long shift = 1L << index;

                switch (c) {
                    case 'k':
                        black.kings |= shift;
                        break;
                    case 'K':
                        white.kings |= shift;
                        break;
                    case 'q':
                        black.queens |= shift;
                        break;
                    case 'Q':
                        white.queens |= shift;
                        break;
                    case 'r':
                        black.rooks |= shift;
                        break;
                    case 'R':
                        white.rooks |= shift;
                        break;
                    case 'b':
                        black.bishops |= shift;
                        break;
                    case 'B':
                        white.bishops |= shift;
                        break;
                    case 'n':
                        black.knights |= shift;
                        break;
                    case 'N':
                        white.knights |= shift;
                        break;
                    case 'p':
                        black.pawns |= shift;
                        break;
                    case 'P':
                        white.pawns |= shift;
                        break;
                }

                lineIndex++;
            }
        }
    }

    // endregion

    // region State Accessors
    //     _____ _______    _______ ______            _____ _____ ______  _____ _____  ____  _____   _____
    //    / ____|__   __|/\|__   __|  ____|     /\   / ____/ ____|  ____|/ ____/ ____|/ __ \|  __ \ / ____|
    //   | (___    | |  /  \  | |  | |__       /  \ | |   | |    | |__  | (___| (___ | |  | | |__) | (___
    //    \___ \   | | / /\ \ | |  |  __|     / /\ \| |   | |    |  __|  \___ \\___ \| |  | |  _  / \___ \
    //    ____) |  | |/ ____ \| |  | |____   / ____ \ |___| |____| |____ ____) |___) | |__| | | \ \ ____) |
    //   |_____/   |_/_/    \_\_|  |______| /_/    \_\_____\_____|______|_____/_____/ \____/|_|  \_\_____/

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public int getFullmoveClock() {
        return fullmoveClock;
    }

    public Color getActivePlayer() {
        return turn;
    }

    public boolean canCastleKingSide(final Color color) {
        Objects.requireNonNull(color);

        return color == Color.WHITE ? white.kingSideCastle : black.kingSideCastle;
    }

    public boolean canCastleQueenSide(final Color color) {
        Objects.requireNonNull(color);

        return color == Color.WHITE ? white.queenSideCastle : black.queenSideCastle;
    }

    public Square getEnPassant() {
        return enPassant == 0L ? null : SQUARES[Long.numberOfTrailingZeros(enPassant)];
    }

    // endregion

    // region Move Generator
    //    __  __  ______      ________    _____ ______ _   _ ______ _____         _______ ____  _____
    //   |  \/  |/ __ \ \    / /  ____|  / ____|  ____| \ | |  ____|  __ \     /\|__   __/ __ \|  __ \
    //   | \  / | |  | \ \  / /| |__    | |  __| |__  |  \| | |__  | |__) |   /  \  | | | |  | | |__) |
    //   | |\/| | |  | |\ \/ / |  __|   | | |_ |  __| | . ` |  __| |  _  /   / /\ \ | | | |  | |  _  /
    //   | |  | | |__| | \  /  | |____  | |__| | |____| |\  | |____| | \ \  / ____ \| | | |__| | | \ \
    //   |_|  |_|\____/   \/   |______|  \_____|______|_| \_|______|_|  \_\/_/    \_\_|  \____/|_|  \_\

    @Deprecated
    public List<MoveResult> generateValidMoves() {
        return new MoveGenerator(false)
                .getPseudoLegalMoves()
                .stream()
                .map(this::copyMake)
                .collect(Collectors.toList());
    }

    private MoveResult copyMake(final BBMove move) {
        final Bitboard copy = new Bitboard(this);

        copy.make(move);

        final Piece promotePiece = PIECES[((int) ((move.bits & PROMOTION_PIECE_MASK) >> PROMOTION_PIECE_SHIFT))];

        final ColoredPiece promoteTo = promotePiece == null ? null : promotePiece.ofColor(turn.opposite());

        return new MoveResult(copy, new Move(
                SQUARES[Long.numberOfTrailingZeros((move.bits & SOURCE_SQUARE_INDEX_MASK) >> SOURCE_SQUARE_INDEX_SHIFT)],
                SQUARES[Long.numberOfTrailingZeros((move.bits & TARGET_SQUARE_INDEX_MASK) >> TARGET_SQUARE_INDEX_SHIFT)],
                null,
                promoteTo,
                false,
                false,
                false
        ));
    }

    public static boolean hasAnyLegalMoves(final Bitboard board, final Collection<BBMove> moves) {
        for (final BBMove move : moves) {
            board.make(move);

            if (!board.isInvalidPosition()) {
                board.unmake(move);

                return true;
            }

            board.unmake(move);
        }

        return false;
    }

    public static boolean hasAnyAttackMoves(final Collection<BBMove> moves) {
        for (final BBMove pseudoLegalMove : moves) {
            if (pseudoLegalMove.isAttack()) {
                return true;
            }
        }

        return false;
    }

    public List<BBMove> generatePseudoLegalMoves() {
        return new MoveGenerator(false).getPseudoLegalMoves();
    }

    public List<BBMove> generatePseudoLegalAttackMoves() {
        return new MoveGenerator(true).getPseudoLegalMoves();
    }

    private class MoveGenerator {
        private final boolean onlyAttackMoves;
        private final List<BBMove> result;

        MoveGenerator(final boolean onlyAttackMoves) {
            this.onlyAttackMoves = onlyAttackMoves;
            this.result = new ArrayList<>();
        }

        List<BBMove> getPseudoLegalMoves() {
            final PlayerBoard self;
            final long selfOccupancy;
            final long opponentOccupancy;

            if (turn == Color.WHITE) {
                self = white;
                selfOccupancy = white.occupancy();
                opponentOccupancy = black.occupancy();
            } else {
                self = black;
                selfOccupancy = black.occupancy();
                opponentOccupancy = white.occupancy();
            }

            final long occupancy = selfOccupancy | opponentOccupancy;

            slidingAttacks(self.queens, occupancy, selfOccupancy, MagicBitboard.ROOK, QUEEN);
            slidingAttacks(self.rooks, occupancy, selfOccupancy, MagicBitboard.ROOK, ROOK);
            slidingAttacks(self.queens, occupancy, selfOccupancy, MagicBitboard.BISHOP, QUEEN);
            slidingAttacks(self.bishops, occupancy, selfOccupancy, MagicBitboard.BISHOP, BISHOP);
            singleAttacks(self.knights, selfOccupancy, KNIGHT_ATTACKS, KNIGHT);
            singleAttacks(self.kings, selfOccupancy, KING_ATTACKS, KING);
            pawnAttacks(self.pawns, selfOccupancy, opponentOccupancy);
            pawnMoves(self.pawns, occupancy);
            castleMoves(self, occupancy);

            return result;
        }

        private void castleMoves(
                final PlayerBoard self,
                final long occupancy
        ) {
            if (turn == Color.WHITE && !isInCheck(Color.WHITE, Square.E1.getOccupiedBitMask(), black, occupancy)) {
                if (self.queenSideCastle
                        && (WHITE_QUEEN_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                        && !isInCheck(Color.WHITE, Square.C1.getOccupiedBitMask(), black, occupancy)
                        && !isInCheck(Color.WHITE, Square.D1.getOccupiedBitMask(), black, occupancy)
                ) {
                    makeCastleMove(Square.E1, Square.C1);
                }

                if (self.kingSideCastle
                        && (WHITE_KING_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                        && !isInCheck(Color.WHITE, Square.F1.getOccupiedBitMask(), black, occupancy)
                        && !isInCheck(Color.WHITE, Square.G1.getOccupiedBitMask(), black, occupancy)
                ) {
                    makeCastleMove(Square.E1, Square.G1);
                }
            } else if (turn == Color.BLACK && !isInCheck(Color.BLACK, Square.E8.getOccupiedBitMask(), white, occupancy)) {
                if (self.queenSideCastle
                        && (BLACK_QUEEN_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                        && !isInCheck(Color.BLACK, Square.C8.getOccupiedBitMask(), white, occupancy)
                        && !isInCheck(Color.BLACK, Square.D8.getOccupiedBitMask(), white, occupancy)
                ) {
                    makeCastleMove(Square.E8, Square.C8);
                }

                if (self.kingSideCastle
                        && (BLACK_KING_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                        && !isInCheck(Color.BLACK, Square.F8.getOccupiedBitMask(), white, occupancy)
                        && !isInCheck(Color.BLACK, Square.G8.getOccupiedBitMask(), white, occupancy)
                ) {
                    makeCastleMove(Square.E8, Square.G8);
                }
            }
        }

        private void makeCastleMove(final Square kingSource, final Square kingTarget) {
            makeBbMove(kingSource.getOccupiedBitMask(), kingTarget.getOccupiedBitMask(), KING, true, false, NO_PIECE, 0L);
        }

        private void pawnMoves(
                final long pawns,
                final long fullOccupancy
        ) {
            long remainingPawns = pawns;

            while (remainingPawns != 0L) {
                final long source = Long.highestOneBit(remainingPawns);
                remainingPawns &= ~source;

                final long singleMoveTarget;
                final long promoteRank;

                final boolean whiteTurn = turn == Color.WHITE;

                if (whiteTurn) {
                    singleMoveTarget = source << 8;
                    promoteRank = RANK_EIGHT_SQUARES;
                } else {
                    singleMoveTarget = source >> 8;
                    promoteRank = RANK_ONE_SQUARES;
                }

                if ((singleMoveTarget & fullOccupancy) == 0L) {
                    if ((singleMoveTarget & promoteRank) == 0L) {
                        //no promotion moves

                        makeBbMove(source, singleMoveTarget, PAWN, false, false, NO_PIECE, NO_SQUARE);

                        final long doubleMoveTarget;
                        final long doubleMoveSourceRank;

                        if (whiteTurn) {
                            doubleMoveTarget = singleMoveTarget << 8;
                            doubleMoveSourceRank = RANK_TWO_SQUARES;
                        } else {
                            doubleMoveTarget = singleMoveTarget >> 8;
                            doubleMoveSourceRank = RANK_SEVEN_SQUARES;
                        }

                        if ((source & doubleMoveSourceRank) != 0L && (doubleMoveTarget & fullOccupancy) == 0L) {
                            //is in starting rank and free double move target square

                            makeBbMove(source, doubleMoveTarget, PAWN, false, false, NO_PIECE, singleMoveTarget);
                        }
                    } else {
                        pawnPromotions(source, singleMoveTarget);
                    }
                }
            }
        }

        private void pawnPromotions(
                final long source,
                final long target
        ) {
            pawnPromotion(source, target, KNIGHT);
            pawnPromotion(source, target, BISHOP);
            pawnPromotion(source, target, ROOK);
            pawnPromotion(source, target, QUEEN);
        }

        private void pawnPromotion(
                final long source,
                final long target,
                final int promotionPiece
        ) {
            makeBbMove(source, target, PAWN, false, false, promotionPiece, 0L);
        }

        private void pawnAttacks(
                final long pawns,
                final long selfOccupancy,
                final long opponentOccupancy
        ) {
            long remainingPawns = pawns;

            final long[] pawnAttacks = turn == Color.WHITE ? WHITE_PAWN_ATTACKS : BLACK_PAWN_ATTACKS;

            while (remainingPawns != 0L) {
                final long source = Long.highestOneBit(remainingPawns);
                remainingPawns &= ~source;

                final long attacks = pawnAttacks[Long.numberOfTrailingZeros(source)] & (opponentOccupancy | enPassant) & ~selfOccupancy;

                generatePawnAttacks(source, attacks);
            }
        }

        private void singleAttacks(
                final long pieces,
                final long selfOccupancy,
                final long[] attacksArray,
                final int piece
        ) {
            long remainingPieces = pieces;

            while (remainingPieces != 0L) {
                final long source = Long.highestOneBit(remainingPieces);
                remainingPieces &= ~source;

                final long attacks = attacksArray[Long.numberOfTrailingZeros(source)] & ~selfOccupancy;

                generateAttacks(source, attacks, piece);
            }
        }

        private void slidingAttacks(
                final long pieces,
                final long fullOccupancy,
                final long selfOccupancy,
                final MagicBitboard bitboard,
                final int piece
        ) {
            long remainingPieces = pieces;

            while (remainingPieces != 0L) {
                final long source = Long.highestOneBit(remainingPieces);
                remainingPieces &= ~source;

                final long attacks = bitboard.attacks(fullOccupancy, Long.numberOfTrailingZeros(source)) & ~selfOccupancy;

                generateAttacks(source, attacks, piece);
            }
        }

        private void generateAttacks(
                final long source,
                final long attacks,
                final int piece
        ) {
            long remainingAttacks = attacks;

            while (remainingAttacks != 0L) {
                final long attack = Long.highestOneBit(remainingAttacks);
                remainingAttacks &= ~attack;

                makeBbMove(source, attack, piece, false, false, NO_PIECE, NO_SQUARE);
            }
        }

        private void generatePawnAttacks(
                final long source,
                final long attacks
        ) {
            long remainingAttacks = attacks;

            while (remainingAttacks != 0L) {
                final long attack = Long.highestOneBit(remainingAttacks);
                remainingAttacks &= ~attack;

                if ((turn == Color.WHITE && (attack & RANK_EIGHT_SQUARES) != 0L) || (turn == Color.BLACK && (attack & RANK_ONE_SQUARES) != 0L)) {
                    pawnPromotions(source, attack);
                } else {
                    makeBbMove(source, attack, PAWN, false, attack == enPassant, NO_PIECE, NO_SQUARE);
                }
            }
        }

        private void makeBbMove(
                final long sourceSquare, final long targetSquare, final int pieceMoved, final boolean castleMove, final boolean enPassantAttack, final int piecePromote, final long enPassantOpportunitySquare
        ) {
            final int sourceSquareIndex = Long.numberOfTrailingZeros(sourceSquare);
            final int targetSquareIndex = Long.numberOfTrailingZeros(targetSquare);

            long bits = 0L;

            final int attackSquareIndex;

            if (enPassantAttack) {
                attackSquareIndex = turn == Color.WHITE ? targetSquareIndex - 8 : targetSquareIndex + 8;
                bits |= EN_PASSANT_ATTACK_MASK;
            } else {
                attackSquareIndex = targetSquareIndex;
            }

            final int pieceAttacked = turn == Color.WHITE ? black.getPieceConst(attackSquareIndex) : white.getPieceConst(attackSquareIndex);

            if (onlyAttackMoves && pieceAttacked == 0) {
                return;
            }

            bits |= (long) pieceMoved << PIECE_MOVED_SHIFT;
            bits |= (long) pieceAttacked << PIECE_ATTACKED_SHIFT;

            bits |= (long) sourceSquareIndex << SOURCE_SQUARE_INDEX_SHIFT;
            bits |= (long) targetSquareIndex << TARGET_SQUARE_INDEX_SHIFT;

            if (castleMove) {
                bits |= CASTLE_MOVE_MASK;
            }

            if (pieceMoved == PAWN || pieceAttacked != NO_PIECE) {
                bits |= HALFMOVE_RESET_MASK;
            }

            bits |= (long) halfmoveClock << PREVIOUS_HALFMOVE_SHIFT;

            if (enPassant != 0L) {
                bits |= (long) Long.numberOfTrailingZeros(enPassant) << PREVIOUS_EN_PASSANT_SQUARE_INDEX_SHIFT;
            }

            if (enPassantOpportunitySquare != 0L) {
                bits |= ((long) Long.numberOfTrailingZeros(enPassantOpportunitySquare)) << NEXT_EN_PASSANT_SQUARE_INDEX_SHIFT;
            }

            bits |= (long) piecePromote << PROMOTION_PIECE_SHIFT;

            if (turn == Color.BLACK) {
                if (white.queenSideCastle && targetSquareIndex == A1) {
                    bits |= OPPONENT_LOST_QUEEN_SIDE_CASTLE_MASK;
                } else if (white.kingSideCastle && targetSquareIndex == H1) {
                    bits |= OPPONENT_LOST_KING_SIDE_CASTLE_MASK;
                }

                if (black.queenSideCastle && (sourceSquareIndex == A8 || sourceSquareIndex == E8)) {
                    bits |= SELF_LOST_QUEEN_SIDE_CASTLE_MASK;
                }

                if (black.kingSideCastle && (sourceSquareIndex == H8 || sourceSquareIndex == E8)) {
                    bits |= SELF_LOST_KING_SIDE_CASTLE_MASK;
                }
            } else {
                if (black.queenSideCastle && targetSquareIndex == A8) {
                    bits |= OPPONENT_LOST_QUEEN_SIDE_CASTLE_MASK;
                } else if (black.kingSideCastle && targetSquareIndex == H8) {
                    bits |= OPPONENT_LOST_KING_SIDE_CASTLE_MASK;
                }

                if (white.queenSideCastle && (sourceSquareIndex == A1 || sourceSquareIndex == E1)) {
                    bits |= SELF_LOST_QUEEN_SIDE_CASTLE_MASK;
                }

                if (white.kingSideCastle && (sourceSquareIndex == H1 || sourceSquareIndex == E1)) {
                    bits |= SELF_LOST_KING_SIDE_CASTLE_MASK;
                }
            }

            final int turnConst = turn == Color.WHITE ? WHITE : BLACK;

            final int gameStage = (white.queens | black.queens) == 0L ? 1 : 0;

            final int squareDiff = PIECE_SQUARE_VALUES[turnConst][pieceMoved][gameStage][targetSquareIndex]
                    - PIECE_SQUARE_VALUES[turnConst][pieceMoved][gameStage][sourceSquareIndex];

            final int mvvLva = mvvLva(pieceMoved, pieceAttacked);

            result.add(new BBMove(bits, mvvLva, squareDiff));
        }
    }

    // endregion

    // region Piece Getters
    //    _____ _____ ______ _____ ______    _____ ______ _______ ______ _____   _____
    //   |  __ \_   _|  ____/ ____|  ____|  / ____|  ____|__   __|__   __|  ____|  __ \ / ____|
    //   | |__) || | | |__ | |    | |__    | |  __| |__     | |     | |  | |__  | |__) | (___
    //   |  ___/ | | |  __|| |    |  __|   | | |_ |  __|    | |     | |  |  __| |  _  / \___ \
    //   | |    _| |_| |___| |____| |____  | |__| | |____   | |     | |  | |____| | \ \ ____) |
    //   |_|   |_____|______\_____|______|  \_____|______|  |_|     |_|  |______|_|  \_\_____/

    public ColoredPiece getPiece(final Square square) {
        return getPiece(square.getOccupiedBitMask());
    }

    private ColoredPiece getPiece(final long square) {
        if ((white.kings & square) != 0L) {
            return ColoredPiece.WHITE_KING;
        }

        if ((white.queens & square) != 0L) {
            return ColoredPiece.WHITE_QUEEN;
        }

        if ((white.rooks & square) != 0L) {
            return ColoredPiece.WHITE_ROOK;
        }

        if ((white.bishops & square) != 0L) {
            return ColoredPiece.WHITE_BISHOP;
        }

        if ((white.knights & square) != 0L) {
            return ColoredPiece.WHITE_KNIGHT;
        }

        if ((white.pawns & square) != 0L) {
            return ColoredPiece.WHITE_PAWN;
        }

        if ((black.kings & square) != 0L) {
            return ColoredPiece.BLACK_KING;
        }

        if ((black.queens & square) != 0L) {
            return ColoredPiece.BLACK_QUEEN;
        }

        if ((black.rooks & square) != 0L) {
            return ColoredPiece.BLACK_ROOK;
        }

        if ((black.bishops & square) != 0L) {
            return ColoredPiece.BLACK_BISHOP;
        }

        if ((black.knights & square) != 0L) {
            return ColoredPiece.BLACK_KNIGHT;
        }

        if ((black.pawns & square) != 0L) {
            return ColoredPiece.BLACK_PAWN;
        }

        return null;
    }

    private static boolean isOccupied(final long board, final long square) {
        return (board & square) != 0L;
    }

    // endregion

    // region Heuristics
    //    _    _ ______ _    _ _____  _____  _____ _______ _____ _____  _____
    //   | |  | |  ____| |  | |  __ \|_   _|/ ____|__   __|_   _/ ____|/ ____|
    //   | |__| | |__  | |  | | |__) | | | | (___    | |    | || |    | (___
    //   |  __  |  __| | |  | |  _  /  | |  \___ \   | |    | || |     \___ \
    //   | |  | | |____| |__| | | \ \ _| |_ ____) |  | |   _| || |____ ____) |
    //   |_|  |_|______|\____/|_|  \_\_____|_____/   |_|  |_____\_____|_____/

    public int computeScore(final Color color) {
        Objects.requireNonNull(color);

        return color == Color.WHITE ? white.score() : black.score();
    }

    public int pieceSquareValue(final Color color) {
        final boolean lateGame = (white.queens | black.queens) == 0L;

        final int[] whiteKingTable = lateGame ? WHITE_KING_TABLE_LATE : WHITE_KING_TABLE_MID;
        final int[] blackKingTable = lateGame ? BLACK_KING_TABLE_LATE : BLACK_KING_TABLE_MID;

        final int whiteSum = sum(white.pawns, WHITE_PAWN_TABLE)
                + sum(white.knights, WHITE_KNIGHT_TABLE)
                + sum(white.bishops, WHITE_BISHOP_TABLE)
                + sum(white.rooks, WHITE_ROOK_TABLE)
                + sum(white.queens, WHITE_QUEEN_TABLE)
                + sum(white.kings, whiteKingTable);

        final int blackSum = sum(black.pawns, BLACK_PAWN_TABLE)
                + sum(black.knights, BLACK_KNIGHT_TABLE)
                + sum(black.bishops, BLACK_BISHOP_TABLE)
                + sum(black.rooks, BLACK_ROOK_TABLE)
                + sum(black.queens, BLACK_QUEEN_TABLE)
                + sum(black.kings, blackKingTable);

        final int sum = whiteSum + blackSum;

        return color == Color.WHITE ? -sum : sum;
    }

    private static int sum(final long board, final int[] valueTable) {
        long occupancy = board;

        int sum = 0;

        while (occupancy != 0L) {
            final long current = Long.highestOneBit(occupancy);
            occupancy &= ~current;
            sum += valueTable[Long.numberOfTrailingZeros(current)];
        }

        return sum;
    }

    private static int mvvLva(final int source, final int target) {
        if (target == NO_PIECE || target == KING) {
            return 0;
        }

        final int sourceValue = source == KING ? QUEEN_VALUE + 1 : pieceValue(source);

        return (pieceValue(target) << 8) - sourceValue;
    }

    public long zobristHash() {
        long occupancy = white.occupancy() | black.occupancy();

        long hash = 0L;

        while (occupancy != 0L) {
            final long current = Long.highestOneBit(occupancy);
            occupancy &= ~current;

            final int squareIndex = Long.numberOfTrailingZeros(current);

            final ColoredPiece piece = getPiece(current);

            hash ^= ZobristHashing.hashPieceSquare(piece, squareIndex);
        }

        if (enPassant != 0L) {
            hash ^= ZobristHashing.hashEnPassant(Long.numberOfTrailingZeros(enPassant));
        }

        if (white.kingSideCastle) {
            hash ^= ZobristHashing.whiteKingCastleHash();
        }
        if (white.queenSideCastle) {
            hash ^= ZobristHashing.whiteQueenCastleHash();
        }
        if (black.kingSideCastle) {
            hash ^= ZobristHashing.blackKingCastleHash();
        }
        if (black.queenSideCastle) {
            hash ^= ZobristHashing.blackQueenCastleHash();
        }

        if (turn == Color.BLACK) {
            hash ^= ZobristHashing.getBlacksTurnHash();
        }

        return hash;
    }

    public boolean zobristEquals(final Bitboard bitboard) {
        return white.equals(bitboard.white) && black.equals(bitboard.black) && enPassant == bitboard.enPassant;
    }

    // endregion

    // region Checks
    //     _____ _    _ ______ _____ _  __ _____
    //    / ____| |  | |  ____/ ____| |/ // ____|
    //   | |    | |__| | |__ | |    | ' /| (___
    //   | |    |  __  |  __|| |    |  <  \___ \
    //   | |____| |  | | |___| |____| . \ ____) |
    //    \_____|_|  |_|______\_____|_|\_\_____/

    public boolean isInvalidPosition() {
        return isInCheck(turn.opposite());
    }

    public boolean isInCheck() {
        return isInCheck(turn);
    }

    public boolean isInCheck(final Color color) {
        Objects.requireNonNull(color);

        if (color == Color.WHITE) {
            return isInCheck(Color.WHITE, black);
        }

        return isInCheck(Color.BLACK, white);
    }

    public boolean isInCheck(final Color color, final Square square) {
        Objects.requireNonNull(color);

        final long occupancy = white.occupancy() | black.occupancy();
        if (color == Color.WHITE) {
            return isInCheck(Color.WHITE, square.getOccupiedBitMask(), black, occupancy);
        }

        return isInCheck(Color.BLACK, square.getOccupiedBitMask(), white, occupancy);
    }

    private boolean isInCheck(final Color color, final PlayerBoard opponent) {
        long selfKings;
        final long occupancy = white.occupancy() | black.occupancy();

        if (color == Color.WHITE) {
            selfKings = white.kings;
        } else {
            selfKings = black.kings;
        }

        while (selfKings != 0L) {
            final long king = Long.highestOneBit(selfKings);
            selfKings &= ~king;

            if (isInCheck(color, king, opponent, occupancy)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isInCheck(final Color color, final long square, final PlayerBoard opponent, final long occupancy) {
        final int index = Long.numberOfTrailingZeros(square);

        final long rookAttacks = MagicBitboard.ROOK.attacks(occupancy, index);

        if ((rookAttacks & (opponent.rooks | opponent.queens)) != 0L) {
            return true;
        }

        final long bishopAttacks = MagicBitboard.BISHOP.attacks(occupancy, index);

        if ((bishopAttacks & (opponent.bishops | opponent.queens)) != 0L) {
            return true;
        }

        final long knightAttacks = KNIGHT_ATTACKS[index];

        if ((knightAttacks & opponent.knights) != 0L) {
            return true;
        }

        final long pawnAttacks;

        if (color == Color.WHITE && (square & RANK_EIGHT_SQUARES) == 0) {
            pawnAttacks = WHITE_PAWN_ATTACKS[index];
        } else if (color == Color.BLACK && (square & RANK_ONE_SQUARES) == 0) {
            pawnAttacks = BLACK_PAWN_ATTACKS[index];
        } else {
            pawnAttacks = 0L;
        }

        if ((pawnAttacks & opponent.pawns) != 0L) {
            return true;
        }

        final long kingAttacks = KING_ATTACKS[index];

        if ((kingAttacks & opponent.kings) != 0L) {
            return true;
        }

        return false;
    }

    // endregion

    // region String Generation
    //     _____ _______ _____  _____ _   _  _____    _____ ______ _   _ ______ _____         _______ _____ ____  _   _
    //    / ____|__   __|  __ \|_   _| \ | |/ ____|  / ____|  ____| \ | |  ____|  __ \     /\|__   __|_   _/ __ \| \ | |
    //   | (___    | |  | |__) | | | |  \| | |  __  | |  __| |__  |  \| | |__  | |__) |   /  \  | |    | || |  | |  \| |
    //    \___ \   | |  |  _  /  | | | . ` | | |_ | | | |_ |  __| | . ` |  __| |  _  /   / /\ \ | |    | || |  | | . ` |
    //    ____) |  | |  | | \ \ _| |_| |\  | |__| | | |__| | |____| |\  | |____| | \ \  / ____ \| |   _| || |__| | |\  |
    //   |_____/   |_|  |_|  \_\_____|_| \_|\_____|  \_____|______|_| \_|______|_|  \_\/_/    \_\_|  |_____\____/|_| \_|

    public String fen() {
        final StringBuilder stringBuilder = new StringBuilder("................................................................");

        for (final Square square : SQUARES) {
            final ColoredPiece piece = getPiece(square);

            if (piece == null) {
                continue;
            }

            final int index = (8 - square.getRank().getIndex() - 1) * 8 + square.getFile().getIndex();

            stringBuilder.setCharAt(index, piece.getSan());
        }

        String result = stringBuilder.toString()
                                     .replaceAll("(?<=\\G.{8})", "/")
                                     .replaceFirst("/$", "");

        while (true) {
            final Pattern compile = Pattern.compile("^[^.]*(\\.+).*$");

            final Matcher matcher = compile.matcher(result);

            if (!matcher.matches()) {
                final StringBuilder castle = new StringBuilder();

                if (white.kingSideCastle) {
                    castle.append("K");
                }

                if (white.queenSideCastle) {
                    castle.append("Q");
                }

                if (black.kingSideCastle) {
                    castle.append("k");
                }

                if (black.queenSideCastle) {
                    castle.append("q");
                }

                if (castle.length() == 0) {
                    castle.append("-");
                }

                final String enPassantString = enPassant == 0L ? "-" : SQUARES[Long.numberOfTrailingZeros(enPassant)].getFen();

                return result + " " + turn.getFen() + " " + castle + " " + enPassantString + " " + halfmoveClock + " " + fullmoveClock;
            }

            final String group = matcher.group(1);

            result = result.replaceFirst(Pattern.quote(group), Integer.toString(group.length()));
        }
    }

    @Override
    public String toString() {
        final StringJoiner resultJoiner = new StringJoiner("\n");

        resultJoiner.add("╔═══╦═══╤═══╤═══╤═══╤═══╤═══╤═══╤═══╗");

        final StringJoiner lineJoiner = new StringJoiner("\n║   ╟───┼───┼───┼───┼───┼───┼───┼───╢\n");

        for (int i = 8 - 1; i >= 0; i--) {
            final StringJoiner squareJoiner = new StringJoiner(" │ ");
            for (int j = 0; j < 8; j++) {
                final ColoredPiece piece = getPiece(Square.get(j, i));
                squareJoiner.add(piece == null ? " " : Character.toString(piece.getSan()));
            }
            lineJoiner.add("║ " + (i + 1) + " ║ " + squareJoiner.toString() + " ║");
        }

        resultJoiner.add(lineJoiner.toString());
        resultJoiner.add("║   ╚═══╧═══╧═══╧═══╧═══╧═══╧═══╧═══╣");
        resultJoiner.add("║     A   B   C   D   E   F   G   H ║");
        resultJoiner.add("╠═══════════════════════════════════╣");
        addLine(resultJoiner, "turn", turn.toString());
        addLine(resultJoiner, "halfmove clock", Integer.toString(halfmoveClock));
        addLine(resultJoiner, "fullmove clock", Integer.toString(fullmoveClock));
        addLine(resultJoiner, "castle", Fen.parse(fen()).getCastlingAvailability());
        addLine(resultJoiner, "enPassant", enPassant == 0L ? "-" : SQUARES[Long.numberOfTrailingZeros(enPassant)].toString());
        resultJoiner.add("╚═══════════════════════════════════╝");

        return resultJoiner.toString();
    }

    private static void addLine(final StringJoiner resultJoiner, final String name, final String value) {
        final String enPassantString = name + padLeft(value, 33 - name.length());
        resultJoiner.add("║ " + enPassantString + " ║");
    }

    private static String padRight(final String s, final int n) {
        return String.format("%-" + n + "s", s);
    }

    private static String padLeft(final String s, final int n) {
        return String.format("%" + n + "s", s);
    }

    // endregion

    // region Move Do/Undo
    //    __  __  ______      ________   _____   ____     ___    _ _   _ _____   ____
    //   |  \/  |/ __ \ \    / /  ____| |  __ \ / __ \   / / |  | | \ | |  __ \ / __ \
    //   | \  / | |  | \ \  / /| |__    | |  | | |  | | / /| |  | |  \| | |  | | |  | |
    //   | |\/| | |  | |\ \/ / |  __|   | |  | | |  | |/ / | |  | | . ` | |  | | |  | |
    //   | |  | | |__| | \  /  | |____  | |__| | |__| / /  | |__| | |\  | |__| | |__| |
    //   |_|  |_|\____/   \/   |______| |_____/ \____/_/    \____/|_| \_|_____/ \____/

    public void make(final BBMove bbMove) {
        final PlayerBoard self;
        final PlayerBoard opponent;

        final boolean whiteTurn = turn == Color.WHITE;

        if (whiteTurn) {
            self = white;
            opponent = black;
        } else {
            self = black;
            opponent = white;

            fullmoveClock += 1;
        }

        final long bits = bbMove.bits;

        if ((bits & SELF_LOST_KING_SIDE_CASTLE_MASK) != 0L) {
            self.kingSideCastle = false;
        }

        if ((bits & SELF_LOST_QUEEN_SIDE_CASTLE_MASK) != 0L) {
            self.queenSideCastle = false;
        }

        if ((bits & OPPONENT_LOST_KING_SIDE_CASTLE_MASK) != 0L) {
            opponent.kingSideCastle = false;
        }

        if ((bits & OPPONENT_LOST_QUEEN_SIDE_CASTLE_MASK) != 0L) {
            opponent.queenSideCastle = false;
        }

        final int sourceSquareIndex = (int) ((bits & SOURCE_SQUARE_INDEX_MASK) >> SOURCE_SQUARE_INDEX_SHIFT);
        final int targetSquareIndex = (int) ((bits & TARGET_SQUARE_INDEX_MASK) >> TARGET_SQUARE_INDEX_SHIFT);

        final long sourceSquare = 1L << sourceSquareIndex;
        final long targetSquare = 1L << targetSquareIndex;

        if ((bits & CASTLE_MOVE_MASK) != 0L) {
            if (targetSquare == Square.C1.getOccupiedBitMask()) {
                doCastle(self, Square.A1, Square.E1, Square.D1, Square.C1);
            } else if (targetSquare == Square.G1.getOccupiedBitMask()) {
                doCastle(self, Square.H1, Square.E1, Square.F1, Square.G1);
            } else if (targetSquare == Square.C8.getOccupiedBitMask()) {
                doCastle(self, Square.A8, Square.E8, Square.D8, Square.C8);
            } else if (targetSquare == Square.G8.getOccupiedBitMask()) {
                doCastle(self, Square.H8, Square.E8, Square.F8, Square.G8);
            }
        } else if ((bits & EN_PASSANT_ATTACK_MASK) != 0L) {
            self.pawns &= ~sourceSquare;
            self.pawns |= targetSquare;

            if (whiteTurn) {
                opponent.unsetAll(targetSquare >> 8);
            } else {
                opponent.unsetAll(targetSquare << 8);
            }
        } else {
            switch (((int) ((bits & PIECE_MOVED_MASK) >> PIECE_MOVED_SHIFT))) {
                case KING:
                    self.kings = (self.kings & ~sourceSquare) | targetSquare;
                    break;
                case QUEEN:
                    self.queens = (self.queens & ~sourceSquare) | targetSquare;
                    break;
                case ROOK:
                    self.rooks = (self.rooks & ~sourceSquare) | targetSquare;
                    break;
                case BISHOP:
                    self.bishops = (self.bishops & ~sourceSquare) | targetSquare;
                    break;
                case KNIGHT:
                    self.knights = (self.knights & ~sourceSquare) | targetSquare;
                    break;
                case PAWN:
                    final int promote = ((int) ((bits & PROMOTION_PIECE_MASK) >> PROMOTION_PIECE_SHIFT));

                    if (promote == NO_PIECE) {
                        self.pawns = (self.pawns & ~sourceSquare) | targetSquare;
                    } else {
                        self.pawns &= ~sourceSquare;
                        switch (promote) {
                            case QUEEN:
                                self.queens |= targetSquare;
                                break;
                            case ROOK:
                                self.rooks |= targetSquare;
                                break;
                            case BISHOP:
                                self.bishops |= targetSquare;
                                break;
                            case KNIGHT:
                                self.knights |= targetSquare;
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                    }
            }

            opponent.unsetAll(targetSquare);
        }

        final long enPassantSquareIndex = (bits & NEXT_EN_PASSANT_SQUARE_INDEX_MASK) >> NEXT_EN_PASSANT_SQUARE_INDEX_SHIFT;

        if (enPassantSquareIndex != 0L) {
            enPassant = 1L << enPassantSquareIndex;
        } else {
            enPassant = NO_SQUARE;
        }

        if ((bits & HALFMOVE_RESET_MASK) == 0L) {
            halfmoveClock += 1;
        } else {
            halfmoveClock = 0;
        }

        turn = turn.opposite();
    }

    public void unmake(final BBMove bbMove) {
        turn = turn.opposite();

        final long bits = bbMove.bits;

        halfmoveClock = ((int) ((bits & PREVIOUS_HALFMOVE_MASK) >> PREVIOUS_HALFMOVE_SHIFT));

        final long enPassantSquareIndex = (bits & PREVIOUS_EN_PASSANT_SQUARE_INDEX_MASK) >> PREVIOUS_EN_PASSANT_SQUARE_INDEX_SHIFT;

        if (enPassantSquareIndex != 0L) {
            enPassant = 1L << enPassantSquareIndex;
        } else {
            enPassant = 0L;
        }

        final PlayerBoard self;
        final PlayerBoard opponent;

        final boolean whiteTurn = turn == Color.WHITE;
        if (whiteTurn) {
            self = white;
            opponent = black;
        } else {
            self = black;
            opponent = white;

            fullmoveClock -= 1;
        }

        if ((bits & SELF_LOST_KING_SIDE_CASTLE_MASK) != 0L) {
            self.kingSideCastle = true;
        }

        if ((bits & SELF_LOST_QUEEN_SIDE_CASTLE_MASK) != 0L) {
            self.queenSideCastle = true;
        }

        if ((bits & OPPONENT_LOST_KING_SIDE_CASTLE_MASK) != 0L) {
            opponent.kingSideCastle = true;
        }

        if ((bits & OPPONENT_LOST_QUEEN_SIDE_CASTLE_MASK) != 0L) {
            opponent.queenSideCastle = true;
        }

        final int sourceSquareIndex = (int) ((bits & SOURCE_SQUARE_INDEX_MASK) >> SOURCE_SQUARE_INDEX_SHIFT);
        final int targetSquareIndex = (int) ((bits & TARGET_SQUARE_INDEX_MASK) >> TARGET_SQUARE_INDEX_SHIFT);

        final long sourceSquare = 1L << sourceSquareIndex;
        final long targetSquare = 1L << targetSquareIndex;

        final int pieceMoved = (int) ((bits & PIECE_MOVED_MASK) >> PIECE_MOVED_SHIFT);
        final int pieceAttacked = (int) ((bits & PIECE_ATTACKED_MASK) >> PIECE_ATTACKED_SHIFT);

        if ((bits & CASTLE_MOVE_MASK) != 0L) {
            if (targetSquare == Square.C1.getOccupiedBitMask()) {
                undoCastle(self, Square.A1, Square.E1, Square.D1, Square.C1);
            } else if (targetSquare == Square.G1.getOccupiedBitMask()) {
                undoCastle(self, Square.H1, Square.E1, Square.F1, Square.G1);
            } else if (targetSquare == Square.C8.getOccupiedBitMask()) {
                undoCastle(self, Square.A8, Square.E8, Square.D8, Square.C8);
            } else if (targetSquare == Square.G8.getOccupiedBitMask()) {
                undoCastle(self, Square.H8, Square.E8, Square.F8, Square.G8);
            }
        } else if ((bits & EN_PASSANT_ATTACK_MASK) != 0L) {
            self.pawns |= sourceSquare;
            self.pawns &= ~targetSquare;

            final long epAttackTarget;

            if (whiteTurn) {
                epAttackTarget = targetSquare >> 8;
            } else {
                epAttackTarget = targetSquare << 8;
            }

            switch (pieceAttacked) {
                case KING:
                    opponent.kings |= epAttackTarget;
                    break;
                case QUEEN:
                    opponent.queens |= epAttackTarget;
                    break;
                case ROOK:
                    opponent.rooks |= epAttackTarget;
                    break;
                case BISHOP:
                    opponent.bishops |= epAttackTarget;
                    break;
                case KNIGHT:
                    opponent.knights |= epAttackTarget;
                    break;
                case PAWN:
                    opponent.pawns |= epAttackTarget;
                    break;
            }
        } else {
            switch (pieceAttacked) {
                case KING:
                    opponent.kings |= targetSquare;
                    break;
                case QUEEN:
                    opponent.queens |= targetSquare;
                    break;
                case ROOK:
                    opponent.rooks |= targetSquare;
                    break;
                case BISHOP:
                    opponent.bishops |= targetSquare;
                    break;
                case KNIGHT:
                    opponent.knights |= targetSquare;
                    break;
                case PAWN:
                    opponent.pawns |= targetSquare;
                    break;
            }

            switch (pieceMoved) {
                case KING:
                    self.kings |= sourceSquare;
                    break;
                case QUEEN:
                    self.queens |= sourceSquare;
                    break;
                case ROOK:
                    self.rooks |= sourceSquare;
                    break;
                case BISHOP:
                    self.bishops |= sourceSquare;
                    break;
                case KNIGHT:
                    self.knights |= sourceSquare;
                    break;
                case PAWN:
                    self.pawns |= sourceSquare;
                    break;
            }

            self.unsetAll(targetSquare);
        }
    }

    private static void doCastle(
            final PlayerBoard self,
            final Square rookSource,
            final Square kingSource,
            final Square rookTarget,
            final Square kingTarget
    ) {
        self.rooks &= ~rookSource.getOccupiedBitMask();
        self.kings &= ~kingSource.getOccupiedBitMask();

        self.rooks |= rookTarget.getOccupiedBitMask();
        self.kings |= kingTarget.getOccupiedBitMask();
    }

    private static void undoCastle(
            final PlayerBoard self,
            final Square rookSource,
            final Square kingSource,
            final Square rookTarget,
            final Square kingTarget
    ) {
        self.rooks |= rookSource.getOccupiedBitMask();
        self.kings |= kingSource.getOccupiedBitMask();

        self.rooks &= ~rookTarget.getOccupiedBitMask();
        self.kings &= ~kingTarget.getOccupiedBitMask();
    }

    private static int pieceValue(final int piece) {
        if (piece == NO_PIECE) {
            return 0;
        }

        switch (piece) {
            case KING:
                return KING_VALUE;
            case QUEEN:
                return QUEEN_VALUE;
            case ROOK:
                return ROOK_VALUE;
            case BISHOP:
                return BISHOP_VALUE;
            case KNIGHT:
                return KNIGHT_VALUE;
            case PAWN:
                return PAWN_VALUE;
            default:
                throw new AssertionError();
        }
    }

    @ToString
    public static class BBMove {
        private long bits;

        private final int mvvLva;
        private final int moveOrderValue;

//        private long zobristHashToggle;

        BBMove(final long bits, final int mvvLva, final int squareDiff) {
            this.bits = bits;

            this.mvvLva = mvvLva;
            this.moveOrderValue = mvvLva + squareDiff;
        }

        public UciMove asUciMove() {
            return new UciMove(
                    SQUARES[((int) ((bits & SOURCE_SQUARE_INDEX_MASK) >> SOURCE_SQUARE_INDEX_SHIFT))],
                    SQUARES[((int) ((bits & TARGET_SQUARE_INDEX_MASK) >> TARGET_SQUARE_INDEX_SHIFT))],
                    PIECES[(int) ((bits & PROMOTION_PIECE_MASK) >> PROMOTION_PIECE_SHIFT)]
            );
        }

        public int getMvvLvaSquarePieceDifferenceValue() {
            return moveOrderValue;
        }

        public int getMvvLvaValue() {
            return mvvLva;
        }

        public boolean isAttack() {
            return (bits & PIECE_ATTACKED_MASK) != 0L;
        }
    }

    // endregion

    private static class PlayerBoard {
        private long kings;
        private long queens;
        private long rooks;
        private long bishops;
        private long knights;
        private long pawns;

        private boolean queenSideCastle;
        private boolean kingSideCastle;

        PlayerBoard() {
        }

        PlayerBoard(final PlayerBoard other) {
            this.kings = other.kings;
            this.queens = other.queens;
            this.rooks = other.rooks;
            this.bishops = other.bishops;
            this.knights = other.knights;
            this.pawns = other.pawns;

            this.kingSideCastle = other.kingSideCastle;
            this.queenSideCastle = other.queenSideCastle;
        }

        long occupancy() {
            return kings | queens | rooks | bishops | knights | pawns;
        }

        @Override
        public String toString() {
            final StringJoiner stringJoiner = new StringJoiner("\n");

            stringJoiner.add("***********************");
            stringJoiner.add("KINGS:");
            stringJoiner.add(BitboardUtil.toBoardString(kings));
            stringJoiner.add("QUEENS:");
            stringJoiner.add(BitboardUtil.toBoardString(queens));
            stringJoiner.add("ROOKS:");
            stringJoiner.add(BitboardUtil.toBoardString(rooks));
            stringJoiner.add("BISHOPS:");
            stringJoiner.add(BitboardUtil.toBoardString(bishops));
            stringJoiner.add("KNIGHTS:");
            stringJoiner.add(BitboardUtil.toBoardString(knights));
            stringJoiner.add("PAWNS:");
            stringJoiner.add(BitboardUtil.toBoardString(pawns));
            stringJoiner.add("queenSideCastle = " + queenSideCastle);
            stringJoiner.add("kingSideCastle = " + kingSideCastle);
            stringJoiner.add("***********************");

            return stringJoiner.toString();
        }

        void unsetAll(final long l) {
            final long notL = ~l;

            kings &= notL;
            queens &= notL;
            rooks &= notL;
            bishops &= notL;
            knights &= notL;
            pawns &= notL;
        }

        int score() {
            return Long.bitCount(kings) * KING_VALUE
                    + Long.bitCount(queens) * QUEEN_VALUE
                    + Long.bitCount(rooks) * ROOK_VALUE
                    + Long.bitCount(bishops) * BISHOP_VALUE
                    + Long.bitCount(knights) * KNIGHT_VALUE
                    + Long.bitCount(pawns) * PAWN_VALUE;
        }

        Piece getPiece(final long square) {
            if ((pawns & square) != 0L) {
                return Piece.PAWN;
            }
            if ((knights & square) != 0L) {
                return Piece.KNIGHT;
            }
            if ((bishops & square) != 0L) {
                return Piece.BISHOP;
            }
            if ((rooks & square) != 0L) {
                return Piece.ROOK;
            }
            if ((queens & square) != 0L) {
                return Piece.QUEEN;
            }
            if ((kings & square) != 0L) {
                return Piece.KING;
            }
            return null;
        }

        int getPieceConst(final int squareIndex) {
            return getPieceConst(1L << squareIndex);
        }

        int getPieceConst(final long square) {
            if ((pawns & square) != 0L) {
                return PAWN;
            }
            if ((knights & square) != 0L) {
                return KNIGHT;
            }
            if ((bishops & square) != 0L) {
                return BISHOP;
            }
            if ((rooks & square) != 0L) {
                return ROOK;
            }
            if ((queens & square) != 0L) {
                return QUEEN;
            }
            if ((kings & square) != 0L) {
                return KING;
            }
            return NO_PIECE;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Bitboard bitboard = (Bitboard) o;

        if (enPassant != bitboard.enPassant) return false;
        if (fullmoveClock != bitboard.fullmoveClock) return false;
        if (halfmoveClock != bitboard.halfmoveClock) return false;
        if (black != null ? !black.equals(bitboard.black) : bitboard.black != null) return false;
        if (white != null ? !white.equals(bitboard.white) : bitboard.white != null) return false;
        return turn == bitboard.turn;

    }

    @Override
    public int hashCode() {
        int result = black != null ? black.hashCode() : 0;
        result = 31 * result + (white != null ? white.hashCode() : 0);
        result = 31 * result + (turn != null ? turn.hashCode() : 0);
        result = 31 * result + (int) (enPassant ^ (enPassant >>> 32));
        result = 31 * result + fullmoveClock;
        result = 31 * result + halfmoveClock;
        return result;
    }

    public String bitboardStrings() {
        return "white:\n" + white + "\nblack:" + black;
    }

    public static void main(String[] args) {
        System.out.println(new Bitboard(Fen.parse("r1b2rk1/pp3pp1/5n2/2pp4/8/q3P3/1bQNNPPP/2R1K2R w K - 2 18")).generatePseudoLegalAttackMoves());
    }
}
