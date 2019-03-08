package net.marvk.chess.board;

import java.util.Arrays;
import java.util.EnumMap;
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
                  .collect(Collectors.toMap(ColoredPiece::getSan, Function.identity()));

    private static final Map<Color, Map<Piece, ColoredPiece>> COLOR_PIECE_MAP =
            Arrays.stream(ColoredPiece.values())
                  .collect(Collectors.groupingBy(
                          ColoredPiece::getColor,
                          () -> new EnumMap<>(Color.class),
                          Collectors.toMap(
                                  ColoredPiece::getPiece,
                                  Function.identity(),
                                  (l, r) -> {
                                      throw new AssertionError();
                                  },
                                  () -> new EnumMap<>(Piece.class)
                          )
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

    public static ColoredPiece getPieceFromSan(final char san) {
        return SAN_PIECE_MAP.get(san);
    }

    public static ColoredPiece getPieceFromSan(final String san) {
        if (san == null || san.length() != 1) {
            return null;
        }

        return SAN_PIECE_MAP.get(san.charAt(0));
    }

    public static ColoredPiece getPiece(final Color color, final Piece piece) {
        final Map<Piece, ColoredPiece> pieceColoredPieceMap = COLOR_PIECE_MAP.get(color);

        if (pieceColoredPieceMap == null) {
            return null;
        }

        return pieceColoredPieceMap.get(piece);
    }
}
