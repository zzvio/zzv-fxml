package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import io.zzv.DrawerManager;
import io.zzv.model.PluginJo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TelegramPresenter {
    @FXML
    private View telegramView;

    public void initialize() {
        telegramView.setShowTransitionFactory(BounceInRightTransition::new);

        telegramView.showingProperty()
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

        telegramView.prefHeightProperty().bind(telegramView.heightProperty());
        telegramView.prefWidthProperty().bind(telegramView.widthProperty());

    }

}
