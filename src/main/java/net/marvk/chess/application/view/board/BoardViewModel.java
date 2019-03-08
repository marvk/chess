package net.marvk.chess.application.view.board;

import de.saxsys.mvvmfx.ViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.board.Piece;
import net.marvk.chess.board.*;

import java.util.Optional;
import java.util.Scanner;

@Log4j2
public class BoardViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<Board> board = new ReadOnlyObjectWrapper<>();
    private final GridModel<ColoredPiece> boardGridModel = new GridModel<>();
    private final ObservableList<MoveResult> validMoves = FXCollections.observableArrayList();
    private final Game game;

    public BoardViewModel() {
        boardGridModel.setNumberOfColumns(8);
        boardGridModel.setNumberOfRows(8);

        game = new Game(SimpleCpu::new, SimpleCpu::new);

        board.set(Boards.startingPosition());
        validMoves.setAll(board.get().getValidMoves());

        updateBoard();

        start();
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

    public void move(final Cell<ColoredPiece> source, final Cell<ColoredPiece> target) {
        final Move move = parseMove(source, target);

        log.info("Received move from UI: " + move);

        final Player white = game.getPlayer(Color.WHITE);

        if (white instanceof AsyncPlayer) {
            ((AsyncPlayer) white).setMove(move);
        }

        final Player black = game.getPlayer(Color.BLACK);

        if (black instanceof AsyncPlayer) {
            ((AsyncPlayer) black).setMove(move);
        }
    }

    private Move parseMove(final Cell<ColoredPiece> source, final Cell<ColoredPiece> target) {
        final Square sourceSquare = convert(source);
        final Square targetSquare = convert(target);

        final ColoredPiece piece = source.getState();
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

    public void start() {
        final Thread thread = new Thread(() -> {
            new Scanner(System.in).nextLine();

            while (!game.isGameOver()) {
                final Optional<MoveResult> moveResult = game.nextMove();

                moveResult.ifPresent(result -> Platform.runLater(() -> {
                    board.set(result.getBoard());
                    updateBoard();

                    validMoves.setAll(board.get().getValidMoves());
                }));
            }
        });

        thread.start();
    }

    private static Square convert(final Cell<?> cell) {
        return Square.get(8 - cell.getRow() - 1, cell.getColumn());
    }
}
