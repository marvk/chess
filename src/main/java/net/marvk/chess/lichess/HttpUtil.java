package net.marvk.chess.lichess;

import net.marvk.chess.util.Util;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

public class HttpUtil {
    private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
    private static final String HEADER_AUTHORIZATION_VALUE = "Bearer " + Util.lichessApiToken();

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
}
