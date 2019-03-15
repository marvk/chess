package net.marvk.chess.lichess4j.util;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.nio.CharBuffer;

public final class HttpUtil {
    private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
    private static final String HEADER_AUTHORIZATION_VALUE = "Bearer " + Util.lichessApiToken();

    private HttpUtil() {
        throw new AssertionError("No instances of utility class " + HttpUtil.class);
    }

    public static HttpUriRequest createAuthorizedPostRequest(final String url) {
        return RequestBuilder.post(url)
                             .addHeader(HEADER_AUTHORIZATION_KEY, HEADER_AUTHORIZATION_VALUE)
                             .build();
    }

    public static HttpAsyncRequestProducer createAuthenticatedRequestProducer(final String url) {
        final HttpUriRequest request =
                RequestBuilder.get(url)
                              .addHeader(HEADER_AUTHORIZATION_KEY, HEADER_AUTHORIZATION_VALUE)
                              .build();

        return HttpAsyncMethods.create(request);
    }

    public static String charBufferToString(final CharBuffer buf) {
        final StringBuilder stringBuilder = new StringBuilder();

        while (buf.hasRemaining()) {
            stringBuilder.append(buf.get());
        }

        return stringBuilder.toString();
    }
}
