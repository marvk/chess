package net.marvk.chess.lichess4j;

public class LichessClientOperationException extends LichessClientException {
    public LichessClientOperationException() {
    }

    public LichessClientOperationException(final String message) {
        super(message);
    }

    public LichessClientOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LichessClientOperationException(final Throwable cause) {
        super(cause);
    }

    public LichessClientOperationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
