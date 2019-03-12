package net.marvk.chess.lichess;

import com.google.gson.*;
import lombok.Data;
import net.marvk.chess.board.*;

import java.lang.reflect.Type;
import java.util.Optional;

@Data
public class GameState {
    private final Board board;

    public static class Deserializer implements JsonDeserializer<GameState> {
        private static final String[] EMPTY_STRING_ARRAY = new String[0];

        @Override
        public GameState deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            final String responseType = jsonObject.get("type").getAsString();

            final JsonObject stateObject;

            if ("gameFull".equals(responseType)) {
                stateObject = jsonObject.get("state").getAsJsonObject();
            } else if ("gameState".equals(responseType)) {
                stateObject = jsonObject;
            } else {
                return null;
            }

            final String movesString = stateObject.get("moves").getAsString();

            final String[] moves;

            if (movesString == null || movesString.trim().isEmpty()) {
                moves = EMPTY_STRING_ARRAY;
            } else {
                moves = movesString.split(" ");
            }

            Board board = new SimpleBoard(Fen.STARTING_POSITION);

            for (final String move : moves) {
                final String[] split = move.split("(?<=\\G..)");

                final Square source = Square.getSquareFromFen(split[0]);
                final Square target = Square.getSquareFromFen(split[1]);

                final Piece promotion;

                if (split.length > 2) {
                    promotion = ColoredPiece.getPieceFromSan(split[2].charAt(0)).getPiece();
                } else {
                    promotion = null;
                }

                final Optional<MoveResult> maybeMove =
                        board.getValidMoves()
                             .stream()
                             .filter(m -> m.getMove().getSource() == source)
                             .filter(m -> m.getMove().getTarget() == target)
                             .filter(m -> m.getMove().isPromotion()
                                     ? promotion == m.getMove().getPromoteTo().getPiece()
                                     : promotion == null
                             )
                             .findFirst();

                maybeMove.orElseThrow(IllegalStateException::new);

                board = maybeMove.get().getBoard();
            }

            return new GameState(board);
        }
    }
}
