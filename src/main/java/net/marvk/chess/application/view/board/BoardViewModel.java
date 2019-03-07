package net.marvk.chess.application.view.board;

import de.saxsys.mvvmfx.ViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.chess.board.Piece;
import net.marvk.chess.board.*;

import java.util.Objects;
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

        updateBoard();
    }

    private void updateBoard() {
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
        final Square sourceSquare = convert(source);
        final Square targetSquare = convert(target);

        final ColoredPiece piece = source.getState();
        final Color color = piece.getColor();
        final Rank targetRank = targetSquare.getRank();

        final boolean pawn = piece.getPiece() == Piece.PAWN;

        final boolean possibleBlackPromotion = targetRank == Rank.RANK_1 && color == Color.BLACK;
        final boolean possibleWhitePromotion = targetRank == Rank.RANK_8 && color == Color.WHITE;

        final boolean promotion = pawn && (possibleBlackPromotion || possibleWhitePromotion);

        final Move move;

        if (promotion) {
            move = Move.promotion(sourceSquare, targetSquare, piece, ColoredPiece.getPiece(color, Piece.QUEEN));
        } else {
            move = Move.simple(sourceSquare, targetSquare, piece);
        }

        final Optional<MoveResult> maybeResult =
                validMoves.stream()
                          .filter(possibleMove -> Objects.equals(possibleMove.getMove(), move))
                          .findFirst();

        if (maybeResult.isPresent()) {
            board.set(maybeResult.get().getBoard());
            updateBoard();

            validMoves.setAll(board.get().getValidMoves(Color.BLACK));

            return true;
        }

        return false;
    }

    private static Square convert(final Cell<?> cell) {
        return Square.get(8 - cell.getRow() - 1, cell.getColumn());
    }
}
