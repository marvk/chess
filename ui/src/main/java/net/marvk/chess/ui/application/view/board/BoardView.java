package net.marvk.chess.ui.application.view.board;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridView;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import net.marvk.chess.core.board.Square;

public class BoardView implements FxmlView<BoardViewModel> {
    @FXML
    public Pane rootPane;

    private final GridView<CellViewModel> gridView = new GridView<>();

    @InjectViewModel
    private BoardViewModel viewModel;

    public void initialize() {
        rootPane.getChildren().add(0, gridView);

        gridView.setGridModel(viewModel.getGridModel());

        gridView.setStyle("-fx-background-color: transparent");

        gridView.getStyleClass().add("board-grid");

        final SimpleObjectProperty<Square> hoverSquare = new SimpleObjectProperty<>();

        gridView.setNodeFactory(cell -> new Piece(cell, this::drag, hoverSquare));

        gridView.setOnMouseExited(e -> hoverSquare.set(null));
    }

    private void drag(final Cell<CellViewModel> source, final Cell<CellViewModel> target) {
        if (source.equals(target)) {
            return;
        }

        viewModel.move(source, target);
    }
}
