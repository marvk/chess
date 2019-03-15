package net.marvk.chess.ui.application;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.marvk.chess.ui.application.view.main.MainView;
import net.marvk.chess.ui.application.view.main.MainViewModel;

import java.util.Locale;
import java.util.ResourceBundle;

public class App extends MvvmfxGuiceApplication {
    @Override
    public void startMvvmfx(final Stage stage) {
        final ResourceBundle bundle = ResourceBundle.getBundle("default", Locale.ENGLISH);

        final ViewTuple<MainView, MainViewModel>
                viewTuple = FluentViewLoader.fxmlView(MainView.class)
                                            .resourceBundle(bundle).load();

        final Parent view = viewTuple.getView();

        final Scene scene = new Scene(view);
        stage.setScene(scene);
        stage.show();
    }
}
