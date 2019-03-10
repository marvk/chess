package net.marvk.chess.application.view.board;

import eu.lestard.grid.Cell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.board.Color;
import net.marvk.chess.board.ColoredPiece;
import net.marvk.chess.board.Move;
import net.marvk.chess.board.Square;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

@Log4j2
class Piece extends Pane {
    private static final Map<ColoredPiece, Image> IMAGE_MAP;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");
    private static final BackgroundFill POSSIBLE_MOVE = new BackgroundFill(javafx.scene.paint.Color.BURLYWOOD, new CornerRadii(20), new Insets(10));

    static {
        final Map<ColoredPiece, Image> imageMap = new EnumMap<>(ColoredPiece.class);

        for (final ColoredPiece value : ColoredPiece.values()) {
            final char piece = Character.toLowerCase(value.getSan());
            final String color = value.getColor() == Color.BLACK ? "d" : "l";
            final String fileName = "480px-Chess_" + piece + color + "t45.svg.png";

            final Image image = new Image("/net/marvk/chess/piecesets/cburnett/" + fileName);

            imageMap.put(value, image);
        }

        IMAGE_MAP = Collections.unmodifiableMap(imageMap);

        DECIMAL_FORMAT.setRoundingMode(RoundingMode.DOWN);
    }

    private final Cell<CellViewModel> cell;
    private final PieceDraggedEvent pieceDragged;
    private final SimpleObjectProperty<Square> hoverSquare;
    private final ImageView imageView;
    private final CellViewModel state;
    private final Label scoreLabel;
    private final BackgroundFill backgroundFill;

    public Piece(final Cell<CellViewModel> cell, final PieceDraggedEvent pieceDragged, final SimpleObjectProperty<Square> hoverSquare) {
        this.cell = cell;
        this.pieceDragged = pieceDragged;
        this.hoverSquare = hoverSquare;
        this.scoreLabel = new Label();
        this.scoreLabel.setStyle("-fx-text-fill: black");

        this.state = cell.getState() == null ? CellViewModel.EMPTY : cell.getState();

        final javafx.scene.paint.Color backgroundColor;

        final boolean wasLastSource = state.getSquare() != null && state.getLastMove().getSource() == state.getSquare();
        final boolean wasLastTarget = state.getSquare() != null && state.getLastMove().getTarget() == state.getSquare();

        if (wasLastSource || wasLastTarget) {
            backgroundColor = javafx.scene.paint.Color.ORANGERED;
        } else if ((cell.getRow() + cell.getColumn()) % 2 == 0) {
            backgroundColor = javafx.scene.paint.Color.LEMONCHIFFON;
        } else {
            backgroundColor = javafx.scene.paint.Color.LIGHTSLATEGRAY;
        }

        backgroundFill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        setBackground(new Background(backgroundFill));

        setOnMouseEntered(e -> {
            log.trace(() -> "hovering " + state.getSquare());
            hoverSquare.set(state.getSquare());
        });

        hoverSquare.addListener((observable, oldValue, newValue) -> {
            final List<Move> moves = state.getValidMoves().get(newValue);

            if (moves == null) {
                setBackground(new Background(backgroundFill));
                scoreLabel.setText("");
                return;
            }

            final Optional<Move> targetSquare = moves.stream()
                                                     .filter(m -> m.getSource() == newValue)
                                                     .findFirst();

            setBackground(new Background(backgroundFill, POSSIBLE_MOVE));

            final String value;

            if (targetSquare.isPresent()) {
                final Double score = state.getValues().get(targetSquare.get());

                value = score != null
                        ? DECIMAL_FORMAT.format(score / 1024.)
                        : "null";
            } else {
                value = "";
            }

            scoreLabel.setText(value);
        });

        getChildren().add(scoreLabel);

        if (state.getColoredPiece() == null) {
            imageView = null;
            return;
        }

        final Image image = IMAGE_MAP.get(state.getColoredPiece());
        imageView = new ImageView(image);
        imageView.getStyleClass().add("hand-cursor");
        getStyleClass().add("hand-cursor");

        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        imageView.setSmooth(true);
        imageView.setPickOnBounds(false);

        setOnMouseDragged(this::drag);
        setOnMousePressed(this::drag);
        setOnMouseReleased(this::release);

        getChildren().add(imageView);
    }

    private void drag(final MouseEvent event) {

        getParent().toFront();
        imageView.setManaged(false);
        imageView.setTranslateX(event.getX() - getWidth() / 2);
        imageView.setTranslateY(event.getY() - getHeight() / 2);
    }

    private void release(final MouseEvent event) {
        final Node intersectedNode = event.getPickResult().getIntersectedNode();

        final Node maybePieceNode =
                intersectedNode instanceof ImageView
                        ? intersectedNode.getParent()
                        : intersectedNode;

        if (maybePieceNode instanceof Piece) {
            final Cell<CellViewModel> targetCell = ((Piece) maybePieceNode).cell;

            pieceDragged.dragged(this.cell, targetCell);
        }

        imageView.setManaged(true);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
    }
}
