package net.marvk.chess.application.view.board;

import de.saxsys.mvvmfx.ViewModel;
import eu.lestard.grid.Cell;
import eu.lestard.grid.GridModel;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.board.Piece;
import net.marvk.chess.board.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Log4j2
public class BoardViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<Board> board = new ReadOnlyObjectWrapper<>();
    private final GridModel<CellViewModel> boardGridModel = new GridModel<>();
    private final ObservableList<MoveResult> validMoves = FXCollections.observableArrayList();

    private final SimpleBooleanProperty auto = new SimpleBooleanProperty();

    private final Game game;

    public BoardViewModel() {
        boardGridModel.setNumberOfColumns(8);
        boardGridModel.setNumberOfRows(8);

        game = new Game(SimpleCpu::new, SimpleCpu::new);

        board.set(Boards.startingPosition());
        validMoves.setAll(board.get().getValidMoves());

        auto.addListener((observable, oldValue, newValue) -> {
            if (observable.getValue()) {
                next();
            }
        });

        updateBoard();

        start();
    }

    private void updateBoard() {
        final Map<Move, Double> lastEvaluation;

        final Player player = game.getPlayer(game.getTurn().opposite());

        if (player instanceof LastEvaluationGettable) {
            final Map<Move, Double> map = ((LastEvaluationGettable) player).getLastEvaluation();

            lastEvaluation = map == null ? Collections.emptyMap() : map;
        } else {
            lastEvaluation = Collections.emptyMap();
        }

        for (final Square square : Square.values()) {
            final ColoredPiece piece = board.get().getPiece(square);

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
                    game.getLastMove().getMove()
            );

            boardGridModel.getCell(square.getFile().getIndex(), 8 - square.getRank().getIndex() - 1)
                          .changeState(cellViewModel);
        }
    }

    public Board getBoard() {
        return board.get();
    }

    public ReadOnlyObjectProperty<Board> boardProperty() {
        return board.getReadOnlyProperty();
    }

    public GridModel<CellViewModel> getGridModel() {
        return boardGridModel;
    }

    public void move(final Cell<CellViewModel> source, final Cell<CellViewModel> target) {
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

    private CountDownLatch countDownLatch;

    public void start() {
        final Thread thread = new Thread(() -> {
            while (!game.isGameOver()) {
                final Optional<MoveResult> moveResult = game.nextMove();
                Platform.runLater(this::updateBoard);

                if (!auto.get()) {
                    countDownLatch = new CountDownLatch(1);
                    try {
                        countDownLatch.await();
                    } catch (final InterruptedException e) {
                        log.error(e);
                        Thread.currentThread().interrupt();
                    }
                }

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

    public void next() {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public SimpleBooleanProperty autoProperty() {
        return auto;
    }
}
