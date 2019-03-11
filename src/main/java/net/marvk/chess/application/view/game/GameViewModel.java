package net.marvk.chess.application.view.game;

import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.application.view.board.BoardStateViewModel;
import net.marvk.chess.board.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@Log4j2
public class GameViewModel implements ViewModel {
    private final SimpleObjectProperty<BoardStateViewModel> boardState = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty auto = new SimpleBooleanProperty();

    private final Game game;

    private CountDownLatch countDownLatch;

    public GameViewModel() {
        game = new Game(
                (color) -> new AlphaBetaPlayer(color, new SimpleHeuristic(), 5),
                (color) -> new AlphaBetaPlayer(color, new SimpleHeuristic(), 5)
        );

        auto.addListener((observable, oldValue, newValue) -> {
            if (observable.getValue()) {
                next();
            }
        });

        updateBoard();

        start();
    }

    public SimpleBooleanProperty autoProperty() {
        return auto;
    }

    public void setMove(final Move move) {
        final Player white = game.getPlayer(Color.WHITE);

        if (white instanceof AsyncPlayer) {
            ((AsyncPlayer) white).setMove(move);
        }

        final Player black = game.getPlayer(Color.BLACK);

        if (black instanceof AsyncPlayer) {
            ((AsyncPlayer) black).setMove(move);
        }
    }

    public void start() {
        final Thread thread = new Thread(() -> {
            log.info("Start game loop");

            while (!game.isGameOver()) {
                final Board lastBoard = game.getBoard();

                final Optional<MoveResult> moveResult = game.nextMove();
                Platform.runLater(() -> boardState.set(new BoardStateViewModel(
                        lastBoard,
                        game.getLastMove().getMove(),
                        lastEvaluation(),
                        0.
                )));

                if (!auto.get()) {
                    countDownLatch = new CountDownLatch(1);
                    try {
                        countDownLatch.await();
                    } catch (final InterruptedException e) {
                        log.error(e);
                        Thread.currentThread().interrupt();
                    }
                }

                moveResult.ifPresent(result -> Platform.runLater(() -> boardState.set(new BoardStateViewModel(
                        game.getBoard(),
                        game.getLastMove().getMove(),
                        lastEvaluation(),
                        0.
                ))));
            }
        });

        thread.start();
    }

    public void updateBoard() {
        boardState.set(new BoardStateViewModel(game.getBoard(), game.getLastMove().getMove(), lastEvaluation(), 0.));
    }

    private Map<Move, Double> lastEvaluation() {

        final Player player = game.getPlayer(game.getTurn().opposite());

        if (player instanceof LastEvaluationGettable) {
            final Map<Move, Double> map = ((LastEvaluationGettable) player).getLastEvaluation();

            return map == null ? Collections.emptyMap() : map;
        } else {
            return Collections.emptyMap();
        }
    }

    public void next() {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public BoardStateViewModel getBoardState() {
        return boardState.get();
    }

    public SimpleObjectProperty<BoardStateViewModel> boardStateProperty() {
        return boardState;
    }
}
