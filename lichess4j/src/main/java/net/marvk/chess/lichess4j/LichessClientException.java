package net.marvk.chess.lichess4j;

public abstract class LichessClientException extends Exception {
    public LichessClientException() {
    }

    public LichessClientException(final String message) {
        super(message);
    }

    public LichessClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LichessClientException(final Throwable cause) {
        super(cause);
    }

    public LichessClientException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
