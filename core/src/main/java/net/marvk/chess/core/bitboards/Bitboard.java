package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bitboard implements Board {
    private static final Square[] SQUARES;
    private static final long[] KNIGHT_ATTACKS;
    private static final long[] KING_ATTACKS;

    private static final long[] WHITE_PAWN_ATTACKS;
    private static final long[] BLACK_PAWN_ATTACKS;

    private static int ep = 0;

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

    private static final long RANK_ONE_SQUARES = getRankSquares(Rank.RANK_1);
    private static final long RANK_TWO_SQUARES = getRankSquares(Rank.RANK_2);

    private static final long RANK_SEVEN_SQUARES = getRankSquares(Rank.RANK_7);
    private static final long RANK_EIGHT_SQUARES = getRankSquares(Rank.RANK_8);

    private EndCondition endCondition;

    private int whiteScore;
    private int blackScore;

    private int scoreDiff;
    private Optional<GameResult> gameResult;

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

    private final PlayerBoard black;
    private final PlayerBoard white;

    private final Color turn;
    private long enPassant = 0L;

    private final int halfmoveClock;
    private final int fullmoveClock;

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

        scoreDiff = whiteScore - blackScore;
    }

    private static final long WHITE_QUEEN_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.B1, Square.C1, Square.D1);
    private static final long WHITE_KING_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.F1, Square.G1);
    private static final long BLACK_QUEEN_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.B8, Square.C8, Square.D8);
    private static final long BLACK_KING_SIDE_CASTLE_OCCUPANCY = bitwiseOr(Square.F8, Square.G8);

    private static long bitwiseOr(final Square... squares) {
        return Arrays.stream(squares).mapToLong(Square::getOccupiedBitMask).reduce(0L, (l1, l2) -> l1 | l2);
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

        nextBoard.setScores();

        return new MoveResult(nextBoard, move);
    }

    @Override
    public ColoredPiece getPiece(final Square square) {
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

    private static boolean isOccupied(final long board, final Square square) {
        return (board & (1L << square.getBitboardIndex())) != 0L;
    }

    @Override
    public ColoredPiece getPiece(final int file, final int rank) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColoredPiece[][] getBoard() {
        throw new UnsupportedOperationException();
    }

    @Override
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

    @Override
    public MoveResult makeSimpleMove(final Move move) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MoveResult makeComplexMove(final Move move, final SquareColoredPiecePair... pairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BoardState getState() {
        return new BoardState(
                turn,
                white.kingSideCastle,
                white.queenSideCastle,
                black.kingSideCastle,
                black.queenSideCastle,
                enPassant == 0L ? null : SQUARES[Long.numberOfTrailingZeros(enPassant)],
                halfmoveClock,
                fullmoveClock
        );
    }

    @Override
    public Optional<GameResult> findGameResult() {
        return gameResult;
    }

    @Override
    public double computeScore(final Map<Piece, Double> scoreMap, final Color color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int computeScore(final Color color) {
        Objects.requireNonNull(color);

        return color == Color.WHITE ? whiteScore : blackScore;
    }

    @Override
    public int scoreDiff() {
        return scoreDiff;
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

    @Override
    public boolean isInCheck(final Color color) {
        return false;
    }

    @Override
    public boolean isInCheck(final Color color, final Square square) {
        return false;
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

    private static Move makeMove(final long source, final long target, final ColoredPiece piece) {
        return Move.simple(SQUARES[Long.numberOfTrailingZeros(source)], SQUARES[Long.numberOfTrailingZeros(target)], piece);
    }

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

                return result + " " + turn.getFen() + " " + castle.toString() + " " + enPassantString + " " + halfmoveClock + " " + fullmoveClock;
            }

            final String group = matcher.group(1);

            result = result.replaceFirst(Pattern.quote(group), Integer.toString(group.length()));
        }
    }

    private static class PlayerBoard {

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
            return Long.bitCount(queens) * 9
                    + Long.bitCount(rooks) * 5
                    + Long.bitCount(bishops) * 3
                    + Long.bitCount(knights) * 3
                    + Long.bitCount(pawns);
        }
    }
}
