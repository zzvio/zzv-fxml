package io.zzv.views;

import java.util.List;

import org.semux.Kernel;
import org.semux.net.Channel;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import io.zzv.model.PluginJo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PluginsPresenter {

  @FXML private View pluginsView;
  @FXML private TableView<PluginJo> pluginsTableView;
  @FXML private TableColumn pluginInfo;
  private ObservableList<PluginJo> itens = FXCollections.observableArrayList();

  public void initialize() {
    pluginsView.setShowTransitionFactory(BounceInRightTransition::new);

    FloatingActionButton fab =
        new FloatingActionButton(
            MaterialDesignIcon.INFO.text,
            e -> {
              pluginsTableView.getItems().clear();
              final List<Channel> channelList = Kernel.getInstance().getChannelManager().getActiveChannels();
              itens.clear();
              int i = 0;
              for (final Channel channel :channelList ) {
                final PluginJo jo = new PluginJo(channel);
                  itens.add(jo);
                System.out.println("#" + (++i) + " - "  + channel.toString() + " #block# " + channel.getRemotePeer().getLatestBlockNumber());
              }
              pluginsTableView.setItems(itens);
              System.out.println("Set Channels size - " + itens.size());
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
              } else if (item.getOutbound().equals("Input")) {
                  setStyle("-fx-background-color: #cdf8f5;");
              } else {
                  setStyle("-fx-background-color: #f4f5ce;");
              }
          }
          };

          row.setOnMouseClicked(event -> {
              PluginJo data = row.getItem();
              Dialog dialog = new Dialog();
              dialog.setTitle(new Label("Channel - " + data.getHost() +  " #block - " + data.getBlock()));
              dialog.setContent(new Label(data.toString()));
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
