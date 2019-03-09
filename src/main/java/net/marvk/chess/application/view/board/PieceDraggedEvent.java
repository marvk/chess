package net.marvk.chess.application.view.board;

import eu.lestard.grid.Cell;

@FunctionalInterface
interface PieceDraggedEvent {
    void dragged(final Cell<CellViewModel> source, final Cell<CellViewModel> target);
}
