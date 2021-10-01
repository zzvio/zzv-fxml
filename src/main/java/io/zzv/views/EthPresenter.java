package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import io.zzv.DrawerManager;
import javafx.fxml.FXML;

public class EthPresenter {
    @FXML
    private View ethView;

    public void initialize() {
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
}
