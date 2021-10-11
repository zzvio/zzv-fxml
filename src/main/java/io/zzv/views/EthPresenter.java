package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import io.zzv.DrawerManager;
import io.zzv.plugins.EthLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class EthPresenter {
    @FXML
    private View ethView;
    EthLoader ethLoader;
    @FXML private Button load;
    @FXML private Button start;
    @FXML private Button stop;

    public void initialize() {
        stop.setDisable(true);
        start.setDisable(true);
        ethView.setShowTransitionFactory(BounceInRightTransition::new);

        ethView.showingProperty()
        .addListener(
        (obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(
                    MaterialDesignIcon.MENU.button(
                            e -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Ethereum");
                DrawerManager.pluginDrawer();
                appBar
                .getActionItems()
                .add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
            }
        });

        ethView.prefHeightProperty().bind(ethView.heightProperty());
        ethView.prefWidthProperty().bind(ethView.widthProperty());

    }
    @FXML
    void Load(){
        System.out.println("Loading BTC plugin 0");
        ethLoader = new EthLoader();
        Thread thread = new Thread(ethLoader);
        thread.start();
        load.setDisable(true);
        start.setDisable(false);
    }

    @FXML
    void Start(){
        System.out.println("Starting BTC plugin");
        ethLoader.start();
        start.setDisable(true);
        stop.setDisable(false);
    }

    @FXML
    void Stop() {
        ethLoader.stopPlugin();
        stop.setDisable(true);
        start.setDisable(false);
    }
}
