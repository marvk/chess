package net.marvk.chess.lichess.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.marvk.chess.lichess.model.UciMove;

import java.lang.reflect.Type;
import java.util.Arrays;

public class UciMoveArrayDeserializer implements JsonDeserializer<UciMove[]> {
    @Override
    public UciMove[] deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Arrays.stream(jsonElement.getAsString().split(" "))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .map(UciMove::parse)
                     .toArray(UciMove[]::new);
    }
}
