package net.marvk.chess.kairukuengine;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.board.*;
import net.marvk.chess.uci4j.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
public class KairukuEngine extends UciEngine {
    private static final String PLY_OPTION = "ply";

    private final ExecutorService executor;

    private Future<Void> calculationFuture;
    private int ply;
    private Board board;

    public KairukuEngine(final UIChannel uiChannel) {
        super(uiChannel);

        this.ply = 7;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void uci() {
        uiChannel.idName("kairuku");
        uiChannel.optionSpin(PLY_OPTION, ply, 1, 7);
    }

    @Override
    public void setDebug(final boolean debug) {

    }

    @Override
    public void isReady() {
        uiChannel.readyOk();
    }

    @Override
    public void setOption(final String name, final String value) {
        if (PLY_OPTION.equals(name)) {
            ply = Integer.parseInt(value);
        }
    }

    @Override
    public void registerLater() {

    }

    @Override
    public void register(final String name, final String code) {

    }

    @Override
    public void uciNewGame() {
        stop();

        board = null;
    }

    @Override
    public void positionFromDefault(final UciMove[] moves) {
        board = UciMove.getBoard(moves);
    }

    @Override
    public void position(final String fenString, final UciMove[] moves) {
        board = UciMove.getBoard(moves, Fen.parse(fenString));
    }

    @Override
    public void go(final Go go) {
        final Color color = board.getActivePlayer();

        final Integer time = color == Color.WHITE ? go.getWhiteTime() : go.getBlackTime();

        if (time == null) {
            ply = 7;
        } else if (time < 200) {
            ply = 3;
        } else if (time < 5_000) {
            ply = 4;
        } else if (time < 30_000) {
            ply = 5;
        } else if (time < 120_000) {
            ply = 6;
        } else {
            ply = 7;
        }

        log.info("time = " + time + "\t\t" + "ply = " + ply);

        final AlphaBetaPlayerExplicit player =
                new AlphaBetaPlayerExplicit(color, new SimpleHeuristic(), ply);

        calculationFuture = executor.submit(() -> {
            final Move play = player.play(new MoveResult(board, Move.NULL_MOVE));

            final UciMove theMove = UciMove.parse(play.getUci());

            final Info info =
                    Info.builder()
                        .nps(((long) player.getLastNps()))
                        .score(new Score(player.getLastRoot().getValue() * 100 / 1024, null, null))
                        .depth(ply)
                        .time(((int) player.getLastDuration().getSeconds()))
                        .generate();

            uiChannel.info(info);
            uiChannel.bestMove(theMove);

            return null;
        });
    }

    @Override
    public void stop() {
        calculationFuture.cancel(true);
    }

    @Override
    public void ponderHit() {

    }

    @Override
    public void quit() {
        calculationFuture.cancel(true);
    }
}
