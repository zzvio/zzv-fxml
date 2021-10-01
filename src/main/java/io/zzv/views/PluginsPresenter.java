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

public class PluginsPresenter {

  @FXML private View pluginsView;
  @FXML private TableView<PluginJo> pluginsTableView;
  @FXML private TableColumn pluginInfo;
  private ObservableList<PluginJo> items = FXCollections.observableArrayList();

  public void initialize() {
    pluginsView.setShowTransitionFactory(BounceInRightTransition::new);


    FloatingActionButton fab =
        new FloatingActionButton(
            MaterialDesignIcon.INFO.text,
            e -> {
              final String[] pluginList = {"Limit Order Book", "Telegram", "BTC", "ETH", "IPFS"};
              for ( String pluginName: pluginList) {
                final PluginJo jo = new PluginJo(pluginName);
                  items.add(jo);
                System.out.println("Adding Plugin" + pluginName  );
              }
              pluginsTableView.setItems(items);
              System.out.println("Set Plugins size - " + items.size());
            });
    fab.showOn(pluginsView);

    pluginsView
        .showingProperty()
        .addListener(
            (obs, oldValue, newValue) -> {
              if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(
                    MaterialDesignIcon.MENU.button(
                        e -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Plugins");
                DrawerManager.pluginDrawer();
                appBar
                    .getActionItems()
                    .add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
              }
            });

      pluginsTableView.prefHeightProperty().bind(pluginsView.heightProperty());
      pluginsTableView.prefWidthProperty().bind(pluginsView.widthProperty());

      pluginsTableView.setRowFactory(tv -> {
          TableRow<PluginJo> row = new TableRow<>(){
          @Override
          public void updateItem(PluginJo item, boolean empty) {
              super.updateItem(item, empty) ;
              if (item == null) {
                  setStyle("");
              } else {
                  setStyle("-fx-background-color: #cdf8f5;");
              }
          }
          };

          row.setOnMouseClicked(event -> {
              PluginJo data = row.getItem();
              Dialog dialog = new Dialog();
              dialog.setTitle(new Label("Plugin - " +  data.getName() ));
              dialog.setContent(new Label(data.getName()));
              Button okButton = new Button("OK");
              okButton.setOnAction(e -> {
                  dialog.hide();
              });
              dialog.getButtons().add(okButton);
              dialog.showAndWait();
              System.out.println(data);
          });
          return row ;
      });

  }
}
