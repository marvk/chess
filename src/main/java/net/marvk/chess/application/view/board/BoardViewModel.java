package net.marvk.chess.application.view.board;

import de.saxsys.mvvmfx.ViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import net.marvk.chess.board.Board;
import net.marvk.chess.board.Boards;
import net.marvk.chess.board.ColoredPiece;
import net.marvk.chess.board.Square;

public class BoardViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<Board> board = new ReadOnlyObjectWrapper<>();
    private final GridModel<ColoredPiece> boardGridModel = new GridModel<>();

    public BoardViewModel() {
        boardGridModel.setNumberOfColumns(8);
        boardGridModel.setNumberOfRows(8);

        final Board board = Boards.startingPosition();

        for (final Square value : Square.values()) {
            final ColoredPiece piece = board.getPiece(value);
            boardGridModel.getCell(value.getFile().getIndex(), value.getRank().getIndex()).changeState(piece);
        }
    }

    public Board getBoard() {
        return board.get();
    }

    public ReadOnlyObjectProperty<Board> boardProperty() {
        return board.getReadOnlyProperty();
    }

    public GridModel<ColoredPiece> getGridModel() {
        return boardGridModel;
    }

    public boolean move(final Cell<ColoredPiece> source, final Cell<ColoredPiece> target) {
        return false;
    }
}
