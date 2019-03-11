package net.marvk.chess.lichess;

import com.google.gson.*;
import lombok.Data;
import lombok.NonNull;

@Data
public class Event {
    private final @NonNull Type type;
    private final @NonNull String id;

    public enum Type {
        CHALLENGE,
        GAME_START
    }

    public static class Deserializer implements JsonDeserializer<Event> {
        @Override
        public Event deserialize(final JsonElement jsonElement, final java.lang.reflect.Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            final String eventType = jsonObject.get("type").getAsString();

            final Type resultType;
            final String id;

            if ("challenge".equals(eventType)) {
                resultType = Type.CHALLENGE;
                id = jsonObject.getAsJsonObject("challenge").get("id").getAsString();
            } else if ("gameStart".equals(eventType)) {
                resultType = Type.GAME_START;
                id = jsonObject.getAsJsonObject("game").get("id").getAsString();
            } else {
                return null;
            }

            return new Event(resultType, id);
        }
    }
}
