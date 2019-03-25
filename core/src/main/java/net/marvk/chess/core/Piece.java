package net.marvk.chess.core;

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

            return ColoredPiece.BLACK_KNIGHT;
        }
    },
    PAWN {
        @Override
        public ColoredPiece ofColor(final Color color) {
            Objects.requireNonNull(color);

            if (color == Color.WHITE) {
                return ColoredPiece.WHITE_PAWN;
            }

            return ColoredPiece.BLACK_PAWN;
        }
    };

    public abstract ColoredPiece ofColor(final Color color);
}
