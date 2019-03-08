package net.marvk.chess.board;

import java.util.concurrent.CountDownLatch;

public class AsyncPlayer extends Player {
    private CountDownLatch latch;
    private Move theMove;

    public AsyncPlayer(final Color color) {
        super(color);
    }

    @Override
    public synchronized Move play(final MoveResult previousMove) {
        latch = new CountDownLatch(1);

        try {
            latch.await();
            latch = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        final Move result = this.theMove;
        theMove = null;

        return result;
    }

    public boolean setMove(final Move move) {
        if (!isAwaitingMove()) {
            return false;
        }

        theMove = move;
        latch.countDown();

        return true;
    }

    private boolean isAwaitingMove() {
        return latch != null;
    }
}
