package net.marvk.chess.board;

import java.util.Objects;

public enum Piece {
    KING {
        @Override
        public ColoredPiece ofColor(final Color color) {
            Objects.requireNonNull(color);

            if (color == Color.WHITE) {
                return ColoredPiece.WHITE_KING;
            }

            return ColoredPiece.BLACK_KING;
        }
    },
    QUEEN {
        @Override
        public ColoredPiece ofColor(final Color color) {
            Objects.requireNonNull(color);

            if (color == Color.WHITE) {
                return ColoredPiece.WHITE_QUEEN;
            }

            return ColoredPiece.BLACK_QUEEN;
        }
    },
    ROOK {
        @Override
        public ColoredPiece ofColor(final Color color) {
            Objects.requireNonNull(color);

            if (color == Color.WHITE) {
                return ColoredPiece.WHITE_ROOK;
            }

            return ColoredPiece.BLACK_ROOK;
        }
    },
    BISHOP {
        @Override
        public ColoredPiece ofColor(final Color color) {
            Objects.requireNonNull(color);

            if (color == Color.WHITE) {
                return ColoredPiece.WHITE_BISHOP;
            }

            return ColoredPiece.BLACK_BISHOP;
        }
    },
    KNIGHT {
        @Override
        public ColoredPiece ofColor(final Color color) {
            Objects.requireNonNull(color);

            if (color == Color.WHITE) {
                return ColoredPiece.WHITE_KNIGHT;
            }

            return ColoredPiece.BLACK_KING;
        }
    },
    PAWN {
        @Override
        public ColoredPiece ofColor(final Color color) {
            Objects.requireNonNull(color);

            if (color == Color.WHITE) {
                return ColoredPiece.WHITE_PAWN;
            }

            return ColoredPiece.BLACK_KING;
        }
    };

    public abstract ColoredPiece ofColor(final Color color);
}
