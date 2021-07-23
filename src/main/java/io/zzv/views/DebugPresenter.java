package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import io.zzv.model.Debug;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DebugPresenter {

    @FXML
    private View debugView;

    @FXML
    private TableView<Debug> debugTableView;
    @FXML
    private TableColumn<Debug, String> groupColumn;
    @FXML
    private TableColumn<Debug, String> descColumn;
    @FXML
    private TableColumn<Debug, Boolean> passedColumn;

    public void initialize() {
        debugView.setShowTransitionFactory(BounceInRightTransition::new);

        FloatingActionButton fab = new FloatingActionButton(MaterialDesignIcon.INFO.text,
                e -> {

            System.out.println("Info");
        });
        fab.showOn(debugView);

        debugView.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Debug View");
                appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e ->
                        System.out.println("Favorite")));
            }
        });

        passedColumn.setText("Test Status");
    }
}
