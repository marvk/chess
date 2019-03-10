package net.marvk.chess.application.view.game;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import net.marvk.chess.application.view.board.BoardView;
import net.marvk.chess.application.view.board.BoardViewModel;

public class GameView implements FxmlView<GameViewModel> {
    @FXML
    public ToggleButton auto;

    @FXML
    public VBox gamePanel;

    @InjectViewModel
    private GameViewModel viewModel;

    public void initialize() {
        viewModel.autoProperty().bind(auto.selectedProperty());

        loadBoard();
    }

    private void loadBoard() {
        final ViewTuple<BoardView, BoardViewModel> viewTuple = FluentViewLoader.fxmlView(BoardView.class).load();

        gamePanel.getChildren().add(viewTuple.getView());
        viewTuple.getViewModel().boardStateProperty().bind(viewModel.boardStateProperty());
    }

    @FXML
    public void next(final ActionEvent actionEvent) {
        viewModel.next();
    }
}
