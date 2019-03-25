package net.marvk.chess.lichess4j.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.marvk.chess.core.UciMove;

import java.lang.reflect.Type;

public class UciMoveArrayDeserializer implements JsonDeserializer<UciMove[]> {
    @Override
    public UciMove[] deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return UciMove.parseLine(jsonElement.getAsString());
    }
}
