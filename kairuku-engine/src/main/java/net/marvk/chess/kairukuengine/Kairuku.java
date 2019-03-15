package net.marvk.chess.kairukuengine;

import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.board.*;
import net.marvk.chess.uci4j.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
public class Kairuku extends UciEngine {
    private static final String PLY_OPTION = "ply";

    private final ExecutorService executor;

    private Future<Void> calculationFuture;
    private int ply;
    private Board board;

    public Kairuku(final UIChannel uiChannel) {
        super(uiChannel);

        this.ply = 3;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void uci() {
        uiChannel.idName("kairuku");
        uiChannel.optionSpin(PLY_OPTION, ply, 1, 7);
    }

    public void setDebug(final boolean debug) {

    }

    public void isReady() {
        uiChannel.readyOk();
    }

    public void setOption(final String name, final String value) {
        if (PLY_OPTION.equals(name)) {
            ply = Integer.parseInt(value);
        }
    }

    public void registerLater() {

    }

    public void register(final String name, final String code) {

    }

    public void uciNewGame() {
        stop();

        board = null;
    }

    public void positionFromDefault(final UciMove[] moves) {
        board = UciMove.getBoard(moves);
    }

    public void position(final String fenString, final UciMove[] moves) {
        board = UciMove.getBoard(moves, Fen.parse(fenString));
    }

    public void go(final Go go) {
        final AlphaBetaPlayerExplicit player =
                new AlphaBetaPlayerExplicit(board.getState().getActivePlayer(), new SimpleHeuristic(), ply);

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

    public void stop() {
        calculationFuture.cancel(true);
    }

    public void ponderHit() {

    }

    public void quit() {
        calculationFuture.cancel(true);
    }
}
