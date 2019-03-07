package net.marvk.chess.application.view.board;

import de.saxsys.mvvmfx.ViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.chess.board.*;

import java.util.List;
import java.util.Optional;

public class BoardViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<Board> board = new ReadOnlyObjectWrapper<>();
    private final GridModel<ColoredPiece> boardGridModel = new GridModel<>();
    private final ObservableList<MoveResult> validMoves = FXCollections.observableArrayList();

    public BoardViewModel() {
        boardGridModel.setNumberOfColumns(8);
        boardGridModel.setNumberOfRows(8);

        board.set(Boards.startingPosition());
        validMoves.setAll(board.get().getValidMoves(Color.BLACK));

        for (final Square value : Square.values()) {
            final ColoredPiece piece = board.get().getPiece(value);
            boardGridModel.getCell(value.getFile().getIndex(), 8 - value.getRank().getIndex() - 1).changeState(piece);
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
        final Move move = new Move(convert(source), convert(target), source.getState());

        final Optional<MoveResult> maybeResult = validMoves.stream().filter(m -> m.getMove().equals(move)).findFirst();

        if (maybeResult.isPresent()) {
            board.set(maybeResult.get().getBoard());

            validMoves.setAll(board.get().getValidMoves(Color.BLACK));

            return true;
        }

        return false;
    }

    private static Square convert(final Cell<?> cell) {
        return Square.get(8 - cell.getRow() - 1, cell.getColumn());
    }
}
