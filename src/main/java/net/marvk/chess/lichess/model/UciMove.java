package net.marvk.chess.lichess.model;

import lombok.Data;
import net.marvk.chess.board.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
public class UciMove {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<=\\G..)");
    private final Square source;
    private final Square target;
    private final Piece promote;

    public static UciMove parse(final String uciMove) {
        final String[] split = SPLIT_PATTERN.split(uciMove);

        final Square source = Square.getSquareFromFen(split[0]);
        final Square target = Square.getSquareFromFen(split[1]);

        final Piece promotion;

        if (split.length > 2) {
            promotion = ColoredPiece.getPieceFromSan(split[2].charAt(0)).getPiece();
        } else {
            promotion = null;
        }

        return new UciMove(source, target, promotion);
    }

    public boolean representsMove(final Move move) {
        return move.getSource() == source &&
                move.getTarget() == target &&
                (!move.isPromotion() && promote == null || move.getPromoteTo().getPiece() == promote);
    }

    public boolean representsMove(final MoveResult moveResult) {
        return representsMove(moveResult.getMove());
    }

    public static Board getBoard(final UciMove[] uciMoves) {
        Board board = new SimpleBoard(Fen.STARTING_POSITION);

        for (final UciMove uciMove : uciMoves) {
            final Optional<MoveResult> maybeMove =
                    board.getValidMoves()
                         .stream()
                         .filter(uciMove::representsMove)
                         .findFirst();

            if (!maybeMove.isPresent()) {
                throw new IllegalStateException("Seemingly the opponent tried play an illegal move, this is probably a bug in the move generator. Move history was " + Arrays
                        .toString(uciMoves));
            }

            board = maybeMove.get().getBoard();
        }

        return board;
    }
}
