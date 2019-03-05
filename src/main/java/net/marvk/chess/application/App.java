package net.marvk.chess.application;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.cdi.MvvmfxCdiApplication;
import eu.lestard.grid.GridView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.marvk.chess.application.view.main.MainView;
import net.marvk.chess.application.view.main.MainViewModel;

import java.util.ResourceBundle;

public class App extends MvvmfxCdiApplication {
    @Override
    public void startMvvmfx(final Stage stage) {
        final ViewTuple<MainView, MainViewModel> viewTuple =
                FluentViewLoader.fxmlView(MainView.class)
                                .resourceBundle(ResourceBundle
                                        .getBundle("default_en")).load();

        final Parent view = viewTuple.getView();

        final Scene scene = new Scene(view);
        stage.setScene(scene);
        stage.show();
    }
}
