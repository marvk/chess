package net.marvk.chess.application.view.board;

import eu.lestard.grid.Cell;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import net.marvk.chess.board.Color;
import net.marvk.chess.board.ColoredPiece;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Piece extends Pane {
    private static final Map<ColoredPiece, Image> IMAGE_MAP;

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
    }

    private final Cell<ColoredPiece> cell;
    private final PieceDraggedEvent pieceDragged;
    private final ImageView imageView;

    public Piece(final ColoredPiece coloredPiece, final Cell<ColoredPiece> cell, final PieceDraggedEvent pieceDragged) {
        this.cell = cell;
        this.pieceDragged = pieceDragged;

        final javafx.scene.paint.Color backgroundColor;

        if ((cell.getRow() + cell.getColumn()) % 2 == 0) {
            backgroundColor = javafx.scene.paint.Color.LEMONCHIFFON;
        } else {
            backgroundColor = javafx.scene.paint.Color.LIGHTSLATEGRAY;
        }

        setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

        if (coloredPiece == null) {
            imageView = null;
            return;
        }

        final Image image = IMAGE_MAP.get(coloredPiece);
        imageView = new ImageView(image);

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
            final Cell<ColoredPiece> targetCell = ((Piece) maybePieceNode).cell;

            pieceDragged.dragged(this.cell, targetCell);
        }

        imageView.setManaged(true);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
    }
}
