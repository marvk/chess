package net.marvk.chess.uci4j;

import net.marvk.chess.core.board.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UciEnginePlayerAdapter extends UciEngine {
    private static final String PLY_OPTION = "ply";
    private int ply = 3;
    private Board board;

    private final ExecutorService executor;
    private Future<Object> calculationFuture;

    public UciEnginePlayerAdapter(final UIChannel uiChannel) {
        super(uiChannel);

        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void uci() {
        uiChannel.idAuthor("Marvin");
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

    }

    @Override
    public void positionFromDefault(final UciMove[] moves) {
        board = UciMove.getBoard(moves);
    }

    @Override
    public void position(final String fenString, final UciMove[] moves) {
        board = new SimpleBoard(Fen.parse(fenString));
    }

    @Override
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
