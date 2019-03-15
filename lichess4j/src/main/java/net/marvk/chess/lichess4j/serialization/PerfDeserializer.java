package net.marvk.chess.lichess4j.serialization;

import com.google.gson.*;
import net.marvk.chess.lichess4j.model.Perf;

import java.lang.reflect.Type;

public class PerfDeserializer implements JsonDeserializer<Perf> {
    private static final Gson GSON = new Gson();

    @Override
    public Perf deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return GSON.fromJson(
                jsonElement.getAsJsonObject()
                           .get("name")
                           .getAsString()
                           .toLowerCase(),
                Perf.class
        );
    }
}
