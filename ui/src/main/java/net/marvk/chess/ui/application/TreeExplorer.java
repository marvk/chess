package net.marvk.chess.ui.application;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.marvk.chess.ui.application.view.treeexplorer.TreeExplorerView;
import net.marvk.chess.ui.application.view.treeexplorer.TreeExplorerViewModel;

import java.util.Locale;
import java.util.ResourceBundle;

public class TreeExplorer extends MvvmfxGuiceApplication {
    @Override
    public void startMvvmfx(final Stage stage) {
        final ResourceBundle bundle = ResourceBundle.getBundle("net.marvk.chess.default", Locale.ENGLISH);

        final ViewTuple<TreeExplorerView, TreeExplorerViewModel>
                viewTuple = FluentViewLoader.fxmlView(TreeExplorerView.class)
                                            .resourceBundle(bundle).load();

        final Parent view = viewTuple.getView();

        final Scene scene = new Scene(view);
        stage.setScene(scene);
        stage.show();
    }
}