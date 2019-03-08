package net.marvk.chess.application.view.board;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.board.ColoredPiece;

public class BoardView implements FxmlView<BoardViewModel> {
    @FXML
    public StackPane rootPane;

    private final GridView<ColoredPiece> gridView = new GridView<>();

    @InjectViewModel
    private BoardViewModel viewModel;

    public void initialize() {
        rootPane.getChildren().add(gridView);

        gridView.setGridModel(viewModel.getGridModel());

        gridView.setStyle("-fx-background-color: transparent");

        gridView.getStyleClass().add("board-grid");

        for (final ColoredPiece value : ColoredPiece.values()) {
            gridView.addNodeMapping(value, cell -> new Piece(value, cell, this::drag));
        }

        gridView.addNodeMapping(null, cell -> new Piece(null, cell, null));
    }

    private void drag(final Cell<ColoredPiece> source, final Cell<ColoredPiece> target) {
        if (source.equals(target)) {
            return;
        }

        viewModel.move(source, target);
    }
}
