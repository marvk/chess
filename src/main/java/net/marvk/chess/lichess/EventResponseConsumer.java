package net.marvk.chess.lichess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

@Log4j2
class EventResponseConsumer extends AsyncCharConsumer<Boolean> {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Event.class, new Event.Deserializer())
                                                      .create();

    private final Consumer<Event> eventConsumer;

    EventResponseConsumer(final Consumer<Event> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    @Override
    protected void onCharReceived(final CharBuffer buf, final IOControl ioControl) throws IOException {
        try {
            final String response = Util.charBufferToString(buf).trim();

            if (response.isEmpty()) {
                log.trace("No new events");
                return;
            }

            log.debug(response);

            Arrays.stream(response.split("\n"))
                  .map(line -> GSON.fromJson(line, Event.class))
                  .forEach(event -> {
                      if (event == null) {
                          log.trace("Received malformed event");
                      } else {
                          log.info("Received event " + event);

                          eventConsumer.accept(event);
                      }
                  });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected void onResponseReceived(final HttpResponse response) {
    }

    @Override
    protected Boolean buildResult(final HttpContext context) {
        return Boolean.TRUE;
    }
}
