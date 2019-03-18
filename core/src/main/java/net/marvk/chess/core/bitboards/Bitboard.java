package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

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

    private static final int[] BLACK_KING_TABLE_EARLY = {
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
    private static final int[] WHITE_KING_TABLE_EARLY;
    private static final int[] WHITE_QUEEN_TABLE;
    private static final int[] WHITE_ROOK_TABLE;
    private static final int[] WHITE_BISHOP_TABLE;
    private static final int[] WHITE_KNIGHT_TABLE;
    private static final int[] WHITE_PAWN_TABLE;

    static {
        WHITE_KING_TABLE_LATE = mirror(BLACK_KING_TABLE_LATE);
        WHITE_KING_TABLE_EARLY = mirror(BLACK_KING_TABLE_EARLY);
        WHITE_QUEEN_TABLE = mirror(BLACK_QUEEN_TABLE);
        WHITE_ROOK_TABLE = mirror(BLACK_ROOK_TABLE);
        WHITE_BISHOP_TABLE = mirror(BLACK_BISHOP_TABLE);
        WHITE_KNIGHT_TABLE = mirror(BLACK_KNIGHT_TABLE);
        WHITE_PAWN_TABLE = mirror(BLACK_PAWN_TABLE);
    }

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

    private EndCondition endCondition;

    private int whiteScore;
    private int blackScore;

    private int whiteNumPieces;
    private int blackNumPieces;

    private int scoreDiff;
    private Optional<GameResult> gameResult;

    private final PlayerBoard black;
    private final PlayerBoard white;

    private final Color turn;
    private long enPassant = 0L;

    private final int fullmoveClock;
    private int halfmoveClock;

    private Bitboard(final Bitboard previous) {
        this.white = new PlayerBoard(previous.white);
        this.black = new PlayerBoard(previous.black);

        this.turn = previous.turn.opposite();
        this.halfmoveClock = previous.halfmoveClock + 1;
        this.fullmoveClock = turn == Color.WHITE ? previous.fullmoveClock + 1 : previous.fullmoveClock;
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

        setScores();
    }

    /**
     * Call this method after constructing the board to precalculate the scores
     */
    private void setScores() {
        whiteScore = white.score();
        blackScore = black.score();

        whiteNumPieces = Long.bitCount(white.occupancy());
        blackNumPieces = Long.bitCount(black.occupancy());

        scoreDiff = whiteScore - blackScore;
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

    public int scoreDiff() {
        return scoreDiff;
    }

    public Optional<GameResult> findGameResult() {
        return gameResult;
    }

    // endregion

    // region Move Generator
    //    __  __  ______      ________    _____ ______ _   _ ______ _____         _______ ____  _____
    //   |  \/  |/ __ \ \    / /  ____|  / ____|  ____| \ | |  ____|  __ \     /\|__   __/ __ \|  __ \
    //   | \  / | |  | \ \  / /| |__    | |  __| |__  |  \| | |__  | |__) |   /  \  | | | |  | | |__) |
    //   | |\/| | |  | |\ \/ / |  __|   | | |_ |  __| | . ` |  __| |  _  /   / /\ \ | | | |  | |  _  /
    //   | |  | | |__| | \  /  | |____  | |__| | |____| |\  | |____| | \ \  / ____ \| | | |__| | | \ \
    //   |_|  |_|\____/   \/   |______|  \_____|______|_| \_|______|_|  \_\/_/    \_\_|  \____/|_|  \_\

    public List<MoveResult> getValidMoves() {
        return getValidMovesForColor(turn);
    }

    public List<MoveResult> getValidMovesForColor(final Color color) {
        Objects.requireNonNull(color);

        final PlayerBoard self;
        final long selfOccupancy;
        final long opponentOccupancy;
        final PlayerBoard opponent;

        if (color == Color.WHITE) {
            self = white;
            selfOccupancy = white.occupancy();
            opponent = this.black;
            opponentOccupancy = black.occupancy();
        } else {
            self = black;
            selfOccupancy = black.occupancy();
            opponent = this.white;
            opponentOccupancy = white.occupancy();
        }

        final long occupancy = selfOccupancy | opponentOccupancy;

        final List<MoveResult> result = new ArrayList<>();

        slidingAttacks(result, MagicBitboard.ROOK, self.queens, occupancy, selfOccupancy, color, Piece.QUEEN);
        slidingAttacks(result, MagicBitboard.ROOK, self.rooks, occupancy, selfOccupancy, color, Piece.ROOK);
        slidingAttacks(result, MagicBitboard.BISHOP, self.queens, occupancy, selfOccupancy, color, Piece.QUEEN);
        slidingAttacks(result, MagicBitboard.BISHOP, self.bishops, occupancy, selfOccupancy, color, Piece.BISHOP);
        singleAttacks(result, self.knights, KNIGHT_ATTACKS, selfOccupancy, color, Piece.KNIGHT);
        singleAttacks(result, self.kings, KING_ATTACKS, selfOccupancy, color, Piece.KING);
        pawnAttacks(result, self.pawns, selfOccupancy, opponentOccupancy, color);
        pawnMoves(result, self.pawns, occupancy, color);
        castleMoves(result, self, color, occupancy);

        if (result.isEmpty()) {
            if (isInCheck(turn, opponent)) {
                endCondition = EndCondition.CHECKMATE;
            } else {
                endCondition = EndCondition.DRAW_BY_STALEMATE;
            }
        } else if (halfmoveClock >= 50) {
            endCondition = EndCondition.DRAW_BY_FIFTY_MOVE_RULE;
        }

        gameResult = endCondition == null
                ? Optional.empty()
                : Optional.of(new GameResult(endCondition == EndCondition.CHECKMATE ? turn.opposite() : null, endCondition));

        return result;
    }

    private void castleMoves(final List<MoveResult> result, final PlayerBoard self, final Color color, final long occupancy) {
        if (color == Color.WHITE && !isInCheck(color, Square.E1.getOccupiedBitMask(), black, occupancy)) {
            if (self.queenSideCastle
                    && (WHITE_QUEEN_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                    && !isInCheck(color, Square.C1.getOccupiedBitMask(), black, occupancy)
                    && !isInCheck(color, Square.D1.getOccupiedBitMask(), black, occupancy)
            ) {
                result.add(makeCastleMove(Square.A1, Square.E1, Square.D1, Square.C1, ColoredPiece.WHITE_KING));
            }

            if (self.kingSideCastle
                    && (WHITE_KING_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                    && !isInCheck(color, Square.F1.getOccupiedBitMask(), black, occupancy)
                    && !isInCheck(color, Square.G1.getOccupiedBitMask(), black, occupancy)
            ) {
                result.add(makeCastleMove(Square.H1, Square.E1, Square.F1, Square.G1, ColoredPiece.WHITE_KING));
            }
        } else if (!isInCheck(color, Square.E8.getOccupiedBitMask(), white, occupancy)) {
            if (self.queenSideCastle
                    && (BLACK_QUEEN_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                    && !isInCheck(color, Square.C8.getOccupiedBitMask(), white, occupancy)
                    && !isInCheck(color, Square.D8.getOccupiedBitMask(), white, occupancy)
            ) {
                result.add(makeCastleMove(Square.A8, Square.E8, Square.D8, Square.C8, ColoredPiece.BLACK_KING));
            }

            if (self.kingSideCastle
                    && (BLACK_KING_SIDE_CASTLE_OCCUPANCY & occupancy) == 0L
                    && !isInCheck(color, Square.F8.getOccupiedBitMask(), white, occupancy)
                    && !isInCheck(color, Square.G8.getOccupiedBitMask(), white, occupancy)
            ) {
                result.add(makeCastleMove(Square.H8, Square.E8, Square.F8, Square.G8, ColoredPiece.BLACK_KING));
            }
        }
    }

    private MoveResult makeCastleMove(final Square rookSource, final Square kingSource, final Square rookTarget, final Square kingTarget, final ColoredPiece piece) {
        final Bitboard board = new Bitboard(this);

        final Move simple = Move.simple(kingSource, kingTarget, piece);

        final PlayerBoard newSelf;

        if (piece.getColor() == Color.WHITE) {
            newSelf = board.white;
        } else {
            newSelf = board.black;
        }

        newSelf.rooks &= ~rookSource.getOccupiedBitMask();
        newSelf.kings &= ~kingSource.getOccupiedBitMask();

        newSelf.rooks |= rookTarget.getOccupiedBitMask();
        newSelf.kings |= kingTarget.getOccupiedBitMask();

        newSelf.queenSideCastle = false;
        newSelf.kingSideCastle = false;

        return makeMoveResult(board, simple);
    }

    private void pawnMoves(
            final List<MoveResult> result,
            final long pawns,
            final long occupancy,
            final Color color
    ) {
        long remainingPawns = pawns;

        while (remainingPawns != 0L) {
            final long source = Long.highestOneBit(remainingPawns);
            remainingPawns &= ~source;

            final long singleMoveTarget;
            final ColoredPiece piece;
            final long promoteRank;

            if (color == Color.WHITE) {
                singleMoveTarget = source << 8;
                promoteRank = RANK_EIGHT_SQUARES;
                piece = ColoredPiece.WHITE_PAWN;
            } else {
                singleMoveTarget = source >> 8;
                promoteRank = RANK_ONE_SQUARES;
                piece = ColoredPiece.BLACK_PAWN;
            }

            if ((singleMoveTarget & occupancy) == 0L) {
                if ((singleMoveTarget & promoteRank) == 0L) {
                    //no promotion moves
                    final Move move = makeMove(source, singleMoveTarget, piece);

                    final Bitboard board = new Bitboard(this);

                    final PlayerBoard self;
                    final PlayerBoard opponent;

                    if (color == Color.WHITE) {
                        self = board.white;
                        opponent = board.black;
                    } else {
                        self = board.black;
                        opponent = board.white;
                    }

                    self.pawns &= ~source;
                    self.pawns |= singleMoveTarget;

                    if (!board.isInCheck(color, opponent)) {
                        result.add(makeMoveResult(board, move));
                    }

                    final long doubleMoveTarget;
                    final long doubleMoveSourceRank;

                    if (color == Color.WHITE) {
                        doubleMoveTarget = singleMoveTarget << 8;
                        doubleMoveSourceRank = RANK_TWO_SQUARES;
                    } else {
                        doubleMoveTarget = singleMoveTarget >> 8;
                        doubleMoveSourceRank = RANK_SEVEN_SQUARES;
                    }

                    if ((source & doubleMoveSourceRank) != 0L && (doubleMoveTarget & occupancy) == 0L) {
                        //is in starting rank and free double move target square

                        final Move doubleMove = makeMove(source, doubleMoveTarget, piece);

                        final Bitboard doubleMoveBoard = new Bitboard(this);

                        final PlayerBoard doubleMoveSelf;
                        final PlayerBoard doubleMoveOpponent;

                        if (color == Color.WHITE) {
                            doubleMoveSelf = doubleMoveBoard.white;
                            doubleMoveOpponent = doubleMoveBoard.black;
                        } else {
                            doubleMoveSelf = doubleMoveBoard.black;
                            doubleMoveOpponent = doubleMoveBoard.white;
                        }

                        doubleMoveSelf.pawns &= ~source;
                        doubleMoveSelf.pawns |= doubleMoveTarget;

                        doubleMoveBoard.enPassant = singleMoveTarget;

                        if (!doubleMoveBoard.isInCheck(color, doubleMoveOpponent)) {
                            result.add(makeMoveResult(doubleMoveBoard, doubleMove));
                        }
                    }
                } else {
                    pawnPromotions(result, color, source, singleMoveTarget, piece);
                }
            }
        }
    }

    private void pawnPromotions(final List<MoveResult> result, final Color color, final long source, final long target, final ColoredPiece piece) {
        if (color == Color.WHITE) {
            pawnPromotion(result, source, target, piece, ColoredPiece.WHITE_KNIGHT);
            pawnPromotion(result, source, target, piece, ColoredPiece.WHITE_BISHOP);
            pawnPromotion(result, source, target, piece, ColoredPiece.WHITE_ROOK);
            pawnPromotion(result, source, target, piece, ColoredPiece.WHITE_QUEEN);
        } else {
            pawnPromotion(result, source, target, piece, ColoredPiece.BLACK_KNIGHT);
            pawnPromotion(result, source, target, piece, ColoredPiece.BLACK_BISHOP);
            pawnPromotion(result, source, target, piece, ColoredPiece.BLACK_ROOK);
            pawnPromotion(result, source, target, piece, ColoredPiece.BLACK_QUEEN);
        }
    }

    private void pawnPromotion(final List<MoveResult> result, final long source, final long target, final ColoredPiece piece, final ColoredPiece promotionPiece) {
        final Move move = Move.promotion(SQUARES[Long.numberOfTrailingZeros(source)], SQUARES[Long.numberOfTrailingZeros(target)], piece, promotionPiece);

        final Bitboard board = new Bitboard(this);

        final PlayerBoard self;
        final PlayerBoard opponent;

        if (piece.getColor() == Color.WHITE) {
            self = board.white;
            opponent = board.black;

            if ((target & RANK_EIGHT_SQUARES) != 0L) {
                opponent.unsetAll(target);
            }

        } else {
            self = board.black;
            opponent = board.white;

            if ((target & RANK_ONE_SQUARES) != 0L) {
                opponent.unsetAll(target);
            }
        }

        self.pawns &= ~source;

        if (promotionPiece.getPiece() == Piece.KNIGHT) {
            self.knights |= target;
        } else if (promotionPiece.getPiece() == Piece.BISHOP) {
            self.bishops |= target;
        } else if (promotionPiece.getPiece() == Piece.ROOK) {
            self.rooks |= target;
        } else if (promotionPiece.getPiece() == Piece.QUEEN) {
            self.queens |= target;
        }

        if (!board.isInCheck(promotionPiece.getColor(), opponent)) {
            result.add(makeMoveResult(board, move));
        }
    }

    private void pawnAttacks(
            final List<MoveResult> result,
            final long pawns,
            final long selfOccupancy,
            final long opponentOccupancy,
            final Color color
    ) {
        long remainingPawns = pawns;

        final long[] pawnAttacks = color == Color.WHITE ? WHITE_PAWN_ATTACKS : BLACK_PAWN_ATTACKS;

        while (remainingPawns != 0L) {
            final long source = Long.highestOneBit(remainingPawns);
            remainingPawns &= ~source;

            final long attacks = pawnAttacks[Long.numberOfTrailingZeros(source)] & (opponentOccupancy | enPassant) & ~selfOccupancy;

            generateAttacks(color, Piece.PAWN, result, source, attacks);
        }
    }

    private void singleAttacks(
            final List<MoveResult> result,
            final long pieces,
            final long[] attacksArray,
            final long selfOccupancy,
            final Color color,
            final Piece piece
    ) {
        long remainingPieces = pieces;

        while (remainingPieces != 0L) {
            final long source = Long.highestOneBit(remainingPieces);
            remainingPieces &= ~source;

            final long attacks = attacksArray[Long.numberOfTrailingZeros(source)] & ~selfOccupancy;

            generateAttacks(color, piece, result, source, attacks);
        }
    }

    private void slidingAttacks(
            final List<MoveResult> result,
            final MagicBitboard bitboard,
            final long pieces,
            final long fullOccupancy,
            final long selfOccupancy,
            final Color color,
            final Piece piece
    ) {
        long remainingPieces = pieces;

        while (remainingPieces != 0L) {
            final long source = Long.highestOneBit(remainingPieces);
            remainingPieces &= ~source;

            final long attacks = bitboard.attacks(fullOccupancy, Long.numberOfTrailingZeros(source)) & ~selfOccupancy;

            generateAttacks(color, piece, result, source, attacks);
        }
    }

    private void generateAttacks(final Color color, final Piece piece, final List<MoveResult> result, final long source, final long attacks) {
        long remainingAttacks = attacks;

        while (remainingAttacks != 0L) {
            final long attack = Long.highestOneBit(remainingAttacks);
            remainingAttacks &= ~attack;

            if (piece == Piece.PAWN) {
                if (color == Color.WHITE && (attack & RANK_EIGHT_SQUARES) != 0L) {
                    pawnPromotions(result, color, source, attack, ColoredPiece.WHITE_PAWN);
                    continue;
                } else if (color == Color.BLACK && (attack & RANK_ONE_SQUARES) != 0L) {
                    pawnPromotions(result, color, source, attack, ColoredPiece.BLACK_PAWN);
                    continue;
                }
            }

            final Bitboard nextBoard = new Bitboard(this);

            final PlayerBoard nextSelf;
            final PlayerBoard nextOpponent;

            if (color == Color.WHITE) {
                nextSelf = nextBoard.white;
                nextOpponent = nextBoard.black;
            } else {
                nextSelf = nextBoard.black;
                nextOpponent = nextBoard.white;
            }

            nextSelf.unsetAll(source);
            nextOpponent.unsetAll(attack);

            if (piece == Piece.QUEEN) {
                nextSelf.queens |= attack;
            } else if (piece == Piece.ROOK) {
                nextSelf.rooks |= attack;

                if (color == Color.WHITE) {
                    if (source == Square.A1.getOccupiedBitMask()) {
                        nextSelf.queenSideCastle = false;
                    } else if (source == Square.H1.getOccupiedBitMask()) {
                        nextSelf.kingSideCastle = false;
                    }
                } else {
                    if (source == Square.A8.getOccupiedBitMask()) {
                        nextSelf.queenSideCastle = false;
                    } else if (source == Square.H8.getOccupiedBitMask()) {
                        nextSelf.kingSideCastle = false;
                    }
                }
            } else if (piece == Piece.BISHOP) {
                nextSelf.bishops |= attack;
            } else if (piece == Piece.KNIGHT) {
                nextSelf.knights |= attack;
            } else if (piece == Piece.KING) {
                nextSelf.kings |= attack;

                nextSelf.kingSideCastle = false;
                nextSelf.queenSideCastle = false;
            } else if (piece == Piece.PAWN) {
                nextSelf.pawns |= attack;

                nextBoard.halfmoveClock = 0;

                if (attack == enPassant) {
                    if (color == Color.WHITE) {
                        nextOpponent.pawns &= ~(enPassant >> 8L);
                    } else {
                        nextOpponent.pawns &= ~(enPassant << 8L);
                    }
                }
            } else {
                throw new IllegalStateException();
            }

            if (!nextBoard.isInCheck(color, nextOpponent)) {
                final Move move = makeMove(source, attack, piece.ofColor(color));
                result.add(makeMoveResult(nextBoard, move));
            }
        }
    }

    private MoveResult makeMoveResult(final Bitboard nextBoard, final Move move) {
        if (move.getColoredPiece().getColor() == Color.BLACK) {
            if (move.getTarget() == Square.A1) {
                nextBoard.white.queenSideCastle = false;
            } else if (move.getTarget() == Square.H1) {
                nextBoard.white.kingSideCastle = false;
            }
        } else {
            if (move.getTarget() == Square.A8) {
                nextBoard.black.queenSideCastle = false;
            } else if (move.getTarget() == Square.H8) {
                nextBoard.black.kingSideCastle = false;
            }
        }

        if (move.getColoredPiece().getPiece() == Piece.PAWN) {
            nextBoard.halfmoveClock = 0;
        }

        nextBoard.setScores();

        if (nextBoard.whiteNumPieces + nextBoard.blackNumPieces != whiteNumPieces + blackNumPieces) {
            nextBoard.halfmoveClock = 0;
        }

        return new MoveResult(nextBoard, move);
    }

    private static Move makeMove(final long source, final long target, final ColoredPiece piece) {
        return Move.simple(SQUARES[Long.numberOfTrailingZeros(source)], SQUARES[Long.numberOfTrailingZeros(target)], piece);
    }

    // endregion

    // region Piece Getters
    //    _____ _____ ______ _____ ______    _____ ______ _______ ______ _____   _____
    //   |  __ \_   _|  ____/ ____|  ____|  / ____|  ____|__   __|  ____|  __ \ / ____|
    //   | |__) || | | |__ | |    | |__    | |  __| |__     | |  | |__  | |__) | (___
    //   |  ___/ | | |  __|| |    |  __|   | | |_ |  __|    | |  |  __| |  _  / \___ \
    //   | |    _| |_| |___| |____| |____  | |__| | |____   | |  | |____| | \ \ ____) |
    //   |_|   |_____|______\_____|______|  \_____|______|  |_|  |______|_|  \_\_____/

    public ColoredPiece getPiece(final Square square) {
        return getPiece(square.getOccupiedBitMask());
    }

    private ColoredPiece getPiece(final long square) {
        if (isOccupied(white.kings, square)) {
            return ColoredPiece.WHITE_KING;
        }

        if (isOccupied(white.queens, square)) {
            return ColoredPiece.WHITE_QUEEN;
        }

        if (isOccupied(white.rooks, square)) {
            return ColoredPiece.WHITE_ROOK;
        }

        if (isOccupied(white.bishops, square)) {
            return ColoredPiece.WHITE_BISHOP;
        }

        if (isOccupied(white.knights, square)) {
            return ColoredPiece.WHITE_KNIGHT;
        }

        if (isOccupied(white.pawns, square)) {
            return ColoredPiece.WHITE_PAWN;
        }

        if (isOccupied(black.kings, square)) {
            return ColoredPiece.BLACK_KING;
        }

        if (isOccupied(black.queens, square)) {
            return ColoredPiece.BLACK_QUEEN;
        }

        if (isOccupied(black.rooks, square)) {
            return ColoredPiece.BLACK_ROOK;
        }

        if (isOccupied(black.bishops, square)) {
            return ColoredPiece.BLACK_BISHOP;
        }

        if (isOccupied(black.knights, square)) {
            return ColoredPiece.BLACK_KNIGHT;
        }

        if (isOccupied(black.pawns, square)) {
            return ColoredPiece.BLACK_PAWN;
        }

        return null;
    }

    private static boolean isOccupied(final long board, final long square) {
        return (board & square) != 0L;
    }

    public ColoredPiece getPiece(final int file, final int rank) {
        return getPiece(Square.get(file, rank));
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

        return color == Color.WHITE ? whiteScore : blackScore;
    }

    public int pieceSquareValue(final Color color) {
        long occupancy = white.occupancy() | black.occupancy();

        boolean lateGame = (white.queens | black.queens) == 0L;

        int sum = 0;

        while (occupancy != 0L) {
            final long current = Long.highestOneBit(occupancy);
            occupancy &= ~current;

            final int squareIndex = Long.numberOfTrailingZeros(current);

            final ColoredPiece piece = getPiece(current);

            final int score = getScore(piece, squareIndex, lateGame);

            sum += score;
        }

        if (color == Color.WHITE) {
            return -sum;
        } else {
            return sum;
        }
    }

    private int getScore(final ColoredPiece piece, final int square, final boolean lateGame) {
        if (piece.getColor() == Color.WHITE) {
            switch (piece.getPiece()) {
                case KING:
                    return lateGame ? WHITE_KING_TABLE_LATE[square] : WHITE_KING_TABLE_EARLY[square];
                case QUEEN:
                    return WHITE_QUEEN_TABLE[square];
                case ROOK:
                    return WHITE_ROOK_TABLE[square];
                case BISHOP:
                    return WHITE_BISHOP_TABLE[square];
                case KNIGHT:
                    return WHITE_KNIGHT_TABLE[square];
                case PAWN:
                    return WHITE_PAWN_TABLE[square];
            }
        } else {
            switch (piece.getPiece()) {
                case KING:
                    return lateGame ? BLACK_KING_TABLE_LATE[square] : BLACK_KING_TABLE_EARLY[square];
                case QUEEN:
                    return BLACK_QUEEN_TABLE[square];
                case ROOK:
                    return BLACK_ROOK_TABLE[square];
                case BISHOP:
                    return BLACK_BISHOP_TABLE[square];
                case KNIGHT:
                    return BLACK_KNIGHT_TABLE[square];
                case PAWN:
                    return BLACK_PAWN_TABLE[square];
            }
        }

        return 0;
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

        return hash;
    }

    // endregion

    // region Checks
    //     _____ _    _ ______ _____ _  __ _____
    //    / ____| |  | |  ____/ ____| |/ // ____|
    //   | |    | |__| | |__ | |    | ' /| (___
    //   | |    |  __  |  __|| |    |  <  \___ \
    //   | |____| |  | | |___| |____| . \ ____) |
    //    \_____|_|  |_|______\_____|_|\_\_____/

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

    public boolean zobristEquals(final Bitboard bitboard) {
        return white.equals(bitboard.white) && black.equals(bitboard.black) && enPassant == bitboard.enPassant;
    }

    static class PlayerBoard {
        private long kings;
        private long queens;
        private long rooks;
        private long bishops;
        private long knights;
        private long pawns;

        private boolean queenSideCastle;
        private boolean kingSideCastle;

        public PlayerBoard() {
        }

        public PlayerBoard(final PlayerBoard other) {
            this.kings = other.kings;
            this.queens = other.queens;
            this.rooks = other.rooks;
            this.bishops = other.bishops;
            this.knights = other.knights;
            this.pawns = other.pawns;

            this.kingSideCastle = other.kingSideCastle;
            this.queenSideCastle = other.queenSideCastle;
        }

        public long occupancy() {
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
            stringJoiner.add("***********************");

            return stringJoiner.toString();
        }

        public void unsetAll(final long l) {
            final long notL = ~l;

            kings &= notL;
            queens &= notL;
            rooks &= notL;
            bishops &= notL;
            knights &= notL;
            pawns &= notL;
        }

        public int score() {
            return Long.bitCount(queens) * 900
                    + Long.bitCount(rooks) * 500
                    + Long.bitCount(bishops) * 330
                    + Long.bitCount(knights) * 320
                    + Long.bitCount(pawns) * 100;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final PlayerBoard that = (PlayerBoard) o;

            if (kings != that.kings) return false;
            if (queens != that.queens) return false;
            if (rooks != that.rooks) return false;
            if (bishops != that.bishops) return false;
            if (knights != that.knights) return false;
            if (pawns != that.pawns) return false;
            if (queenSideCastle != that.queenSideCastle) return false;
            return kingSideCastle == that.kingSideCastle;

        }

        @Override
        public int hashCode() {
            int result = (int) (kings ^ (kings >>> 32));
            result = 31 * result + (int) (queens ^ (queens >>> 32));
            result = 31 * result + (int) (rooks ^ (rooks >>> 32));
            result = 31 * result + (int) (bishops ^ (bishops >>> 32));
            result = 31 * result + (int) (knights ^ (knights >>> 32));
            result = 31 * result + (int) (pawns ^ (pawns >>> 32));
            result = 31 * result + (queenSideCastle ? 1 : 0);
            result = 31 * result + (kingSideCastle ? 1 : 0);
            return result;
        }
    }
}
