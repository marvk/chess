package net.marvk.chess.application.view.board;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.lestard.grid.GridView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import net.marvk.chess.board.Board;

public class BoardView implements FxmlView<BoardViewModel> {
    @FXML
    public StackPane rootPane;

    private final GridView<Board> gridView;

    @InjectViewModel
    private BoardViewModel viewModel;

    public BoardView() {
        gridView = new GridView<>();
    }

    public void initialize() {
        gridView.setGridModel(viewModel.getGridModel());
    }
}
