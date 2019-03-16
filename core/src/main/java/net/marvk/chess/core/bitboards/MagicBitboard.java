package net.marvk.chess.core.bitboards;

import net.marvk.chess.core.board.*;

import java.util.*;
import java.util.stream.Collectors;

public class MagicBitboard implements Board {
    private static final Square[] SQUARES;

    static {
        SQUARES = new Square[64];

        for (final Square square : Square.values()) {
            SQUARES[square.getBitboardIndex()] = square;
        }
    }

    private final PlayerBoard black;
    private final PlayerBoard white;

    public MagicBitboard(final PlayerBoard white, final PlayerBoard black) {
        this.white = new PlayerBoard(white);
        this.black = new PlayerBoard(black);
    }

    public MagicBitboard(final Fen fen) {
        this.white = new PlayerBoard();
        this.black = new PlayerBoard();

        loadFen(fen);
    }

    private List<MoveResult> attacks(
            final Bitboard bitboard,
            final long pieces,
            final long fullOccupancy,
            final long selfOccupancy,
            final Color color,
            final Piece piece
    ) {
        long remainingPieces = pieces;

        final ArrayList<MoveResult> result = new ArrayList<>();

        while (remainingPieces != 0L) {
            final long source = Long.highestOneBit(remainingPieces);
            remainingPieces &= ~source;

            long remainingAttacks = bitboard.attacks(fullOccupancy, Long.numberOfTrailingZeros(source)) & ~selfOccupancy;

            while (remainingAttacks != 0L) {
                final long attack = Long.highestOneBit(remainingAttacks);
                remainingAttacks &= ~attack;

                final MagicBitboard nextBoard = new MagicBitboard(this.white, this.black);

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
                } else {
                    throw new IllegalStateException();
                }

                if (!nextBoard.isInCheck(color, opponent)) {
                    result.add(new MoveResult(nextBoard, makeMove(source, attack, piece.ofColor(color))));
                }
            }
        }

        return result;
    }

    private Move makeMove(final long source, final long target, final ColoredPiece piece) {
        return Move.simple(SQUARES[Long.numberOfTrailingZeros(source)], SQUARES[Long.numberOfTrailingZeros(target)], piece);
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
                        System.out.println("lineIndex = " + lineIndex);
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

        MagicBitboards.printBoardString(occupancy);

        final List<List<MoveResult>> attacks = List.of(
                attacks(Bitboard.ROOK, self.queens, occupancy, selfOccupancy, color, Piece.QUEEN),
                attacks(Bitboard.ROOK, self.rooks, occupancy, selfOccupancy, color, Piece.ROOK),
                attacks(Bitboard.BISHOP, self.queens, occupancy, selfOccupancy, color, Piece.QUEEN),
                attacks(Bitboard.BISHOP, self.bishops, occupancy, selfOccupancy, color, Piece.BISHOP)
        );

        return attacks.stream().flatMap(Collection::stream).collect(Collectors.toList());
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
        return new BoardState(Fen.STARTING_POSITION);
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

            final long rookAttacks = Bitboard.ROOK.attacks(occupancy, index);
            final long bishopAttacks = Bitboard.BISHOP.attacks(occupancy, index);

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

    private static class PlayerBoard {
        private long kings;
        private long queens;
        private long rooks;
        private long bishops;
        private long knights;
        private long pawns;

        public PlayerBoard() {
        }

        public PlayerBoard(final PlayerBoard other) {
            this.kings = other.kings;
            this.queens = other.queens;
            this.rooks = other.rooks;
            this.bishops = other.bishops;
            this.knights = other.knights;
            this.pawns = other.pawns;
        }

        public long occupancy() {
            return kings | queens | rooks | bishops | knights | pawns;
        }

        @Override
        public String toString() {
            final StringJoiner stringJoiner = new StringJoiner("\n");

            stringJoiner.add("***********************");
            stringJoiner.add("KINGS:");
            stringJoiner.add(MagicBitboards.toBoardString(kings));
            stringJoiner.add("QUEENS:");
            stringJoiner.add(MagicBitboards.toBoardString(queens));
            stringJoiner.add("ROOKS:");
            stringJoiner.add(MagicBitboards.toBoardString(rooks));
            stringJoiner.add("BISHOPS:");
            stringJoiner.add(MagicBitboards.toBoardString(bishops));
            stringJoiner.add("KNIGHTS:");
            stringJoiner.add(MagicBitboards.toBoardString(knights));
            stringJoiner.add("PAWNS:");
            stringJoiner.add(MagicBitboards.toBoardString(pawns));
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
