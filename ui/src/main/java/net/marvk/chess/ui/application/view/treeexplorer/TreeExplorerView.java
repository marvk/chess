package net.marvk.chess.ui.application.view.treeexplorer;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import net.marvk.chess.core.board.Board;
import net.marvk.chess.core.board.Fen;
import net.marvk.chess.core.board.Move;
import net.marvk.chess.core.board.SimpleHeuristic;
import net.marvk.chess.ui.application.view.board.BoardStateViewModel;
import net.marvk.chess.ui.application.view.board.BoardView;
import net.marvk.chess.ui.application.view.board.BoardViewModel;

public class TreeExplorerView implements FxmlView<net.marvk.chess.ui.application.view.treeexplorer.TreeExplorerViewModel> {
    @FXML
    public TreeView<BoardStateViewModel> nodeTree;

    @FXML
    public SplitPane splitPane;

    @FXML
    public TextField fenStringInput;

    @FXML
    public Spinner<Integer> depthSpinner;

    @InjectViewModel
    private net.marvk.chess.ui.application.view.treeexplorer.TreeExplorerViewModel viewModel;

    public void initialize() {
        viewModel.rootTreeItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nodeTree.setRoot(newValue);
                nodeTree.getSelectionModel().select(0);
            }
        });

        depthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));

        nodeTree.setCellFactory(param -> new TextFieldTreeCell<>(new BoardStateViewModelStringConverter()));

        nodeTree.setRoot(viewModel.getRootTreeItem());

        loadBoard();
    }

    private void loadBoard() {
        final ViewTuple<BoardView, BoardViewModel> tuple = FluentViewLoader.fxmlView(BoardView.class).load();

        splitPane.getItems().add(tuple.getView());

        nodeTree.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> tuple.getViewModel().setBoardState(newValue.getValue())
                );
    }

    public void loadFen(final ActionEvent actionEvent) {
        final boolean valid = Fen.isValid(fenStringInput.getText());

        if (valid) {
            fenStringInput.setBorder(null);
            viewModel.set(Fen.parse(fenStringInput.getText()), depthSpinner.getValue());
        } else {
            fenStringInput.setBorder(new Border(new BorderStroke(Color.ORANGERED, BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }
    }

    private static class BoardStateViewModelStringConverter extends StringConverter<BoardStateViewModel> {
        @Override
        public String toString(final BoardStateViewModel viewModel) {
            final Move lastMove = viewModel.getLastMove();

            if (lastMove.equals(Move.NULL_MOVE)) {
                final Board newBoard = viewModel.getNewBoard();
                final net.marvk.chess.core.board.Color color = newBoard.getActivePlayer();
                return color + " -> [" + (new SimpleHeuristic().evaluate(newBoard, color) / 1024.0) + "]";
            }

            return lastMove.getColoredPiece().getColor() + " -> " + lastMove.getSource().getFen()
                    + lastMove.getTarget().getFen()
                    + "[" + viewModel.getEvaluation() / 1024.0 + "]";
        }

        @Override
        public BoardStateViewModel fromString(final String string) {
            return null;
        }
    }
}
