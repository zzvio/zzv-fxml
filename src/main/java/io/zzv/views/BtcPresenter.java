package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import io.zzv.DrawerManager;
import javafx.fxml.FXML;

public class BtcPresenter {
    @FXML
    private View btcView;

    public void initialize() {
        btcView.setShowTransitionFactory(BounceInRightTransition::new);

        btcView.showingProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {
                            if (newValue) {
                                AppBar appBar = MobileApplication.getInstance().getAppBar();
                                appBar.setNavIcon(
                                        MaterialDesignIcon.MENU.button(
                                                e -> MobileApplication.getInstance().getDrawer().open()));
                                appBar.setTitleText("Telegram");
                                DrawerManager.pluginDrawer();
                                appBar
                                        .getActionItems()
                                        .add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
                            }
                        });

        btcView.prefHeightProperty().bind(btcView.heightProperty());
        btcView.prefWidthProperty().bind(btcView.widthProperty());

    }
}
