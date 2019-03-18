package net.marvk.chess.ui.application.view.game;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import net.marvk.chess.ui.model.PlayerFactory;
import net.marvk.chess.ui.application.view.board.BoardView;
import net.marvk.chess.ui.application.view.board.BoardViewModel;

public class GameView implements FxmlView<GameViewModel> {
    @FXML
    public ToggleButton auto;

    @FXML
    public VBox gamePanel;

    @FXML
    public ChoiceBox<PlayerFactoryChoiceBoxViewModel> whiteChoiceBox;

    @FXML
    public ChoiceBox<PlayerFactoryChoiceBoxViewModel> blackChoiceBox;

    @FXML
    public Button startButton;

    @FXML
    public Button abortButton;

    @InjectViewModel
    private GameViewModel viewModel;

    public void initialize() {
        viewModel.autoProperty().bind(auto.selectedProperty());

        loadBoard();

        initializeDropdown(whiteChoiceBox, viewModel.whiteFactoryProperty());
        initializeDropdown(blackChoiceBox, viewModel.blackFactoryProperty());

        startButton.disableProperty().bind(viewModel.gameInProgressProperty());
        abortButton.disableProperty().bind(
                viewModel.gameInProgressProperty().not().and(viewModel.abortGameProperty().not())
        );

        abortButton.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    private void loadBoard() {
        final ViewTuple<BoardView, BoardViewModel> viewTuple = FluentViewLoader.fxmlView(BoardView.class).load();

        gamePanel.getChildren().add(viewTuple.getView());
        final BoardViewModel boardViewModel = viewTuple.getViewModel();
        boardViewModel.boardStateProperty().bind(this.viewModel.boardStateProperty());
        boardViewModel.lastUiMoveProperty().addListener((observable, oldValue, newValue) -> viewModel.move(newValue));
    }

    @FXML
    public void next(final ActionEvent actionEvent) {
        viewModel.next();
    }

    @FXML
    public void start(final ActionEvent actionEvent) {
        viewModel.start();
    }

    private void initializeDropdown(final ChoiceBox<PlayerFactoryChoiceBoxViewModel> choiceBox, final SimpleObjectProperty<PlayerFactory> factoryProperty) {
        choiceBox.disableProperty().bind(viewModel.gameInProgressProperty());

        choiceBox.setItems(GameViewModel.playerFactoryChoiceBoxViewModels());
        choiceBox.getSelectionModel().select(0);
        choiceBox.setConverter(new PlayerFactoryChoiceBoxViewModelStringConverter());
        factoryProperty.bind(
                Bindings.createObjectBinding(
                        () -> choiceBox.getSelectionModel()
                                       .selectedItemProperty()
                                       .getValue()
                                       .getPlayerFactory(),
                        choiceBox.getSelectionModel().selectedItemProperty())
        );
    }

    @FXML
    public void abort(final ActionEvent actionEvent) {
        viewModel.abort();
    }

    private static class PlayerFactoryChoiceBoxViewModelStringConverter extends StringConverter<PlayerFactoryChoiceBoxViewModel> {
        @Override
        public String toString(final PlayerFactoryChoiceBoxViewModel object) {
            return object.getName();
        }

        @Override
        public PlayerFactoryChoiceBoxViewModel fromString(final String string) {
            return null;
        }
    }
}
