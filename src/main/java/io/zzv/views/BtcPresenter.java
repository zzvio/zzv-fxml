package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import io.zzv.DrawerManager;
import io.zzv.plugins.BtcLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class BtcPresenter {
    @FXML
    private View btcView;
    BtcLoader btcLoader;
    @FXML private Button load;
    @FXML private Button start;
    @FXML private Button stop;

    public void initialize() {
        stop.setDisable(true);
        start.setDisable(true);
        btcView.setShowTransitionFactory(BounceInRightTransition::new);

        btcView.showingProperty()
        .addListener(
        (obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(
                    MaterialDesignIcon.MENU.button(
                            e -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Bitcoin");
                DrawerManager.pluginDrawer();
                appBar
                .getActionItems()
                .add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
            }
        });

        btcView.prefHeightProperty().bind(btcView.heightProperty());
        btcView.prefWidthProperty().bind(btcView.widthProperty());
    }

    @FXML
    void Load(){
        System.out.println("Loading BTC plugin 0");
        btcLoader = new BtcLoader();
        Thread thread = new Thread(btcLoader);
        thread.start();
        load.setDisable(true);
        start.setDisable(false);
    }

    @FXML
    void Start(){
        System.out.println("Starting BTC plugin");
        btcLoader.start();
        start.setDisable(true);
        stop.setDisable(false);
    }

    @FXML
    void Stop() {
        btcLoader.stopPlugin();
        stop.setDisable(true);
        start.setDisable(false);
    }
}
