package net.marvk.chess.lichess4j;

public class LichessClientInstantiationException extends LichessClientException {
    public LichessClientInstantiationException() {
    }

    public LichessClientInstantiationException(final String message) {
        super(message);
    }

    public LichessClientInstantiationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LichessClientInstantiationException(final Throwable cause) {
        super(cause);
    }

    public LichessClientInstantiationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
