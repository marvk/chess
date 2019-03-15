package net.marvk.chess.ui.application.view.board;

import de.saxsys.mvvmfx.ViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.board.Piece;
import net.marvk.chess.core.board.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class BoardViewModel implements ViewModel {
    private final GridModel<CellViewModel> boardGridModel = new GridModel<>();
    private final SimpleObjectProperty<Move> lastUiMove = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<BoardStateViewModel> boardState = new SimpleObjectProperty<>(
            new BoardStateViewModel(new SimpleBoard(Fen.EMPTY_BOARD), null, Collections.emptyMap(), 0.)
    );

    public BoardViewModel() {
        boardGridModel.setNumberOfColumns(8);
        boardGridModel.setNumberOfRows(8);

        boardState.addListener((observable, oldValue, newValue) -> updateBoard(newValue));
    }

    private void updateBoard(final BoardStateViewModel viewModel) {
        if (viewModel == null) {
            log.warn("null passed into updateBoard");
            return;
        }

        final Board newBoard = viewModel.getNewBoard();
        final Map<Move, Double> lastEvaluation = viewModel.getLastEvaluation();
        final Move lastMove = viewModel.getLastMove();

        final List<MoveResult> validMoves = newBoard.getValidMoves();

        for (final Square square : Square.values()) {
            final ColoredPiece piece = newBoard.getPiece(square);

            final Map<Square, List<Move>> squareValidMoves =
                    validMoves.stream()
                              .map(MoveResult::getMove)
                              .filter(m -> m.getTarget() == square)
                              .collect(Collectors.groupingBy(Move::getSource));

            final Map<Move, Double> valueMap =
                    lastEvaluation.entrySet()
                                  .stream()
                                  .filter(kv -> kv.getKey().getTarget() == square)
                                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            final CellViewModel cellViewModel = new CellViewModel(
                    piece,
                    square,
                    valueMap,
                    squareValidMoves,
                    lastMove
            );

            boardGridModel.getCell(square.getFile().getIndex(), 8 - square.getRank().getIndex() - 1)
                          .changeState(cellViewModel);
        }
    }

    public GridModel<CellViewModel> getGridModel() {
        return boardGridModel;
    }

    public void move(final Cell<CellViewModel> source, final Cell<CellViewModel> target) {
        final Move move = parseMove(source, target);

        lastUiMove.set(move);

        log.info("Received move from UI: " + move);
    }

    private Move parseMove(final Cell<CellViewModel> source, final Cell<CellViewModel> target) {
        final Square sourceSquare = convert(source);
        final Square targetSquare = convert(target);

        final ColoredPiece piece = source.getState().getColoredPiece();
        final Color color = piece.getColor();
        final Rank targetRank = targetSquare.getRank();

        final boolean pawn = piece.getPiece() == Piece.PAWN;

        final boolean possibleBlackPromotion = targetRank == Rank.RANK_1 && color == Color.BLACK;
        final boolean possibleWhitePromotion = targetRank == Rank.RANK_8 && color == Color.WHITE;

        final boolean promotion = pawn && (possibleBlackPromotion || possibleWhitePromotion);

        if (promotion) {
            return Move.promotion(sourceSquare, targetSquare, piece, ColoredPiece.getPiece(color, Piece.QUEEN));
        } else {
            return Move.simple(sourceSquare, targetSquare, piece);
        }
    }

    private static Square convert(final Cell<CellViewModel> cell) {
        return cell.getState().getSquare();
    }

    public BoardStateViewModel getBoardState() {
        return boardState.get();
    }

    public SimpleObjectProperty<BoardStateViewModel> boardStateProperty() {
        return boardState;
    }

    public void setBoardState(final BoardStateViewModel boardState) {
        this.boardState.set(boardState);
    }

    public Move getLastUiMove() {
        return lastUiMove.get();
    }

    public SimpleObjectProperty<Move> lastUiMoveProperty() {
        return lastUiMove;
    }
}
