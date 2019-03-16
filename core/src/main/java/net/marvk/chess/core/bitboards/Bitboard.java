package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.*;

import java.util.*;

public class Bitboard implements Board {
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

    private static final long RANK_ONE_SQUARES = getRankSquares(Rank.RANK_1);
    private static final long RANK_TWO_SQUARES = getRankSquares(Rank.RANK_2);

    private static final long RANK_SEVEN_SQUARES = getRankSquares(Rank.RANK_7);
    private static final long RANK_EIGHT_SQUARES = getRankSquares(Rank.RANK_8);

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

        //TODO load en passant and castle

        loadFen(fen);
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
                        result.add(new MoveResult(board, move));

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

                            if (color == Color.WHITE) {
                                doubleMoveSelf = doubleMoveBoard.white;
                            } else {
                                doubleMoveSelf = doubleMoveBoard.black;
                            }

                            doubleMoveSelf.pawns &= ~source;
                            doubleMoveSelf.pawns |= doubleMoveTarget;

                            doubleMoveBoard.enPassant = singleMoveTarget;

                            result.add(new MoveResult(doubleMoveBoard, doubleMove));
                        }
                    }
                } else {
                    //promotion moves
                }
            }
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

            long attacks = attacksArray[Long.numberOfTrailingZeros(source)] & ~selfOccupancy;

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

            long attacks = bitboard.attacks(fullOccupancy, Long.numberOfTrailingZeros(source)) & ~selfOccupancy;

            generateAttacks(color, piece, result, source, attacks);
        }
    }

    private void generateAttacks(final Color color, final Piece piece, final List<MoveResult> result, final long source, final long attacks) {
        long remainingAttacks = attacks;

        while (remainingAttacks != 0L) {
            final long attack = Long.highestOneBit(remainingAttacks);
            remainingAttacks &= ~attack;

            final Bitboard nextBoard = new Bitboard(this);

            final PlayerBoard self;
            final PlayerBoard opponent;

            if (color == Color.WHITE) {
                self = nextBoard.white;
                opponent = nextBoard.black;
            } else {
                self = nextBoard.black;
                opponent = nextBoard.white;
            }

            self.unsetAll(source);
            opponent.unsetAll(attack);

            if (piece == Piece.QUEEN) {
                self.queens |= attack;
            } else if (piece == Piece.ROOK) {
                self.rooks |= attack;
            } else if (piece == Piece.BISHOP) {
                self.bishops |= attack;
            } else if (piece == Piece.KNIGHT) {
                self.knights |= attack;
            } else if (piece == Piece.KING) {
                self.kings |= attack;
            } else if (piece == Piece.PAWN) {
                self.pawns |= attack;

                if (attack == enPassant) {
                    if (color == Color.WHITE) {
                        opponent.pawns &= ~(enPassant >> 8L);
                    } else {
                        opponent.pawns &= ~(enPassant << 8L);
                    }
                }
            } else {
                throw new IllegalStateException();
            }

            if (!nextBoard.isInCheck(color, opponent)) {
                result.add(new MoveResult(nextBoard, makeMove(source, attack, piece.ofColor(color))));
            }
        }
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
    public ColoredPiece getPiece(final Square square) {
        if (occupied(white.kings, square)) {
            return ColoredPiece.WHITE_KING;
        }

        if (occupied(white.queens, square)) {
            return ColoredPiece.WHITE_QUEEN;
        }

        if (occupied(white.rooks, square)) {
            return ColoredPiece.WHITE_ROOK;
        }

        if (occupied(white.bishops, square)) {
            return ColoredPiece.WHITE_BISHOP;
        }

        if (occupied(white.knights, square)) {
            return ColoredPiece.WHITE_KNIGHT;
        }

        if (occupied(white.pawns, square)) {
            return ColoredPiece.WHITE_PAWN;
        }

        if (occupied(black.kings, square)) {
            return ColoredPiece.BLACK_KING;
        }

        if (occupied(black.queens, square)) {
            return ColoredPiece.BLACK_QUEEN;
        }

        if (occupied(black.rooks, square)) {
            return ColoredPiece.BLACK_ROOK;
        }

        if (occupied(black.bishops, square)) {
            return ColoredPiece.BLACK_BISHOP;
        }

        if (occupied(black.knights, square)) {
            return ColoredPiece.BLACK_KNIGHT;
        }

        if (occupied(black.pawns, square)) {
            return ColoredPiece.BLACK_PAWN;
        }

        return null;
    }

    private boolean occupied(final long board, final Square square) {
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

        if (color == Color.WHITE) {
            self = white;
            selfOccupancy = white.occupancy();
            opponentOccupancy = black.occupancy();
        } else {
            self = black;
            selfOccupancy = black.occupancy();
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
                false,
                false,
                false,
                false,
                enPassant == 0L ? null : SQUARES[Long.numberOfTrailingZeros(enPassant)],
                halfmoveClock,
                fullmoveClock
        );
    }

    @Override
    public Optional<GameResult> findGameResult() {
        return Optional.empty();
    }

    @Override
    public double computeScore(final Map<Piece, Double> scoreMap, final Color color) {
        return 0;
    }

    @Override
    public boolean isInCheck(final Color color) {
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

            final int index = Long.numberOfTrailingZeros(king);

            final long rookAttacks = MagicBitboard.ROOK.attacks(occupancy, index);
            final long bishopAttacks = MagicBitboard.BISHOP.attacks(occupancy, index);

            if ((rookAttacks & (opponent.rooks | opponent.queens)) != 0L) {
                return true;
            }

            if ((bishopAttacks & (opponent.bishops | opponent.queens)) != 0L) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isInCheck(final Color color, final Square square) {
        return false;
    }

    private static Move makeMove(final long source, final long target, final ColoredPiece piece) {
        return Move.simple(SQUARES[Long.numberOfTrailingZeros(source)], SQUARES[Long.numberOfTrailingZeros(target)], piece);
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
    }
}
