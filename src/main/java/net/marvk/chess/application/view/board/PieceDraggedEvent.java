package net.marvk.chess.application.view.board;

import eu.lestard.grid.Cell;
import net.marvk.chess.board.ColoredPiece;

@FunctionalInterface
public interface PieceDraggedEvent {
    void dragged(final Cell<ColoredPiece> source, final Cell<ColoredPiece> target);
}
