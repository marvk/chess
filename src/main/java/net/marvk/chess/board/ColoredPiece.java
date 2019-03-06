package net.marvk.chess.board;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ColoredPiece {
    WHITE_KING(Piece.KING, Color.WHITE, 'K'),
    WHITE_QUEEN(Piece.QUEEN, Color.WHITE, 'Q'),
    WHITE_ROOK(Piece.ROOK, Color.WHITE, 'R'),
    WHITE_BISHOP(Piece.BISHOP, Color.WHITE, 'B'),
    WHITE_KNIGHT(Piece.KNIGHT, Color.WHITE, 'N'),
    WHITE_PAWN(Piece.PAWN, Color.WHITE, 'P'),
    BLACK_KING(Piece.KING, Color.BLACK, 'k'),
    BLACK_QUEEN(Piece.QUEEN, Color.BLACK, 'q'),
    BLACK_ROOK(Piece.ROOK, Color.BLACK, 'r'),
    BLACK_BISHOP(Piece.BISHOP, Color.BLACK, 'b'),
    BLACK_KNIGHT(Piece.KNIGHT, Color.BLACK, 'n'),
    BLACK_PAWN(Piece.PAWN, Color.BLACK, 'p');

    private final Piece piece;
    private final Color color;
    private final char san;

    private static final Map<Character, ColoredPiece> SAN_PIECE_MAP =
            Arrays.stream(ColoredPiece.values())
                  .collect(
                          Collectors.collectingAndThen(
                                  Collectors.toMap(ColoredPiece::getSan, Function.identity()),
                                  Collections::unmodifiableMap
                          )
                  );

    ColoredPiece(final Piece piece, final Color color, final char san) {
        this.piece = piece;
        this.color = color;
        this.san = san;
    }

    public Piece getPiece() {
        return piece;
    }

    public Color getColor() {
        return color;
    }

    public char getSan() {
        return san;
    }

    public List<MoveResult> applyStrategy(final MoveStrategy moveStrategy, final Square square, final Board board) {
        switch (this) {
            case WHITE_KING:
                return moveStrategy.whiteKingStrategy(square, board);
            case WHITE_QUEEN:
                return moveStrategy.whiteQueenStrategy(square, board);
            case WHITE_ROOK:
                return moveStrategy.whiteRookStrategy(square, board);
            case WHITE_BISHOP:
                return moveStrategy.whiteBishopStrategy(square, board);
            case WHITE_KNIGHT:
                return moveStrategy.whiteKnightStrategy(square, board);
            case WHITE_PAWN:
                return moveStrategy.whitePawnStrategy(square, board);
            case BLACK_KING:
                return moveStrategy.blackKingStrategy(square, board);
            case BLACK_QUEEN:
                return moveStrategy.blackQueenStrategy(square, board);
            case BLACK_ROOK:
                return moveStrategy.blackRookStrategy(square, board);
            case BLACK_BISHOP:
                return moveStrategy.blackBishopStrategy(square, board);
            case BLACK_KNIGHT:
                return moveStrategy.blackKnightStrategy(square, board);
            case BLACK_PAWN:
                return moveStrategy.blackPawnStrategy(square, board);
            default:
                throw new AssertionError();
        }
    }

    public static ColoredPiece getPieceFromSan(final char san) {
        return SAN_PIECE_MAP.get(san);
    }

    public static ColoredPiece getPieceFromSan(final String san) {
        if (san == null || san.length() != 1) {
            return null;
        }

        return SAN_PIECE_MAP.get(san.charAt(0));
    }
}
