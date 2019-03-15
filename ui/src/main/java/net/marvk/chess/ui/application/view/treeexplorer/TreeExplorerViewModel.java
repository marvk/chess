package net.marvk.chess.ui.application.view.treeexplorer;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import lombok.extern.log4j.Log4j2;
import net.marvk.chess.core.board.*;
import net.marvk.chess.ui.application.view.board.BoardStateViewModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class TreeExplorerViewModel implements ViewModel {
    private final SimpleObjectProperty<AlphaBetaPlayerExplicit.Node> node = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<TreeItem<BoardStateViewModel>> rootTreeItem = new SimpleObjectProperty<>();

    public TreeExplorerViewModel() {
        node.addListener((observable, oldValue, newValue) -> rootTreeItem.set(transform(newValue)));
    }

    public AlphaBetaPlayerExplicit.Node getNode() {
        return node.get();
    }

    public SimpleObjectProperty<AlphaBetaPlayerExplicit.Node> nodeProperty() {
        return node;
    }

    public void setNode(final AlphaBetaPlayerExplicit.Node node) {
        this.node.set(node);
    }

    public TreeItem<BoardStateViewModel> getRootTreeItem() {
        return rootTreeItem.get();
    }

    public SimpleObjectProperty<TreeItem<BoardStateViewModel>> rootTreeItemProperty() {
        return rootTreeItem;
    }

    private TreeItem<BoardStateViewModel> transform(final AlphaBetaPlayerExplicit.Node node) {
        if (node == null) {
            return new TreeItem<>();
        }

        final TreeItem<BoardStateViewModel> result = new TreeItem<>(
                new BoardStateViewModel(
                        node.getCurrentState().getBoard(),
                        node.getCurrentState().getMove(),
                        Collections.emptyMap(),
                        ((double) node.getValue())
                )
        );

        final List<TreeItem<BoardStateViewModel>> children =
                node.getChildren()
                    .stream()
                    .sorted(Comparator.comparing(AlphaBetaPlayerExplicit.Node::getValue).reversed())
                    .map(this::transform)
                    .collect(Collectors.toList());

        result.getChildren().addAll(children);

        return result;
    }

    public void set(final Fen parse, final int depth) {
        final SimpleBoard simpleBoard = new SimpleBoard(parse);
        final AlphaBetaPlayerExplicit alphaBetaPlayer = new AlphaBetaPlayerExplicit(Color.getColorFromFen(parse.getActiveColor()), new SimpleHeuristic(), depth);
        final Move play = alphaBetaPlayer.play(new MoveResult(simpleBoard, Move.NULL_MOVE));

        log.info("Bot played " + play);

        node.set(alphaBetaPlayer.getLastRoot());
    }
}
