package io.zzv.views;

import java.util.List;

import org.checkerframework.checker.units.qual.C;
import org.semux.Kernel;
import org.semux.net.Channel;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import io.zzv.model.ChannelJo;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ChannelsPresenter {

  @FXML private View channelsView;
  @FXML private TableView<ChannelJo> channelsTableView;
  @FXML private TableColumn channelInfo;
  private ObservableList<ChannelJo> itens = FXCollections.observableArrayList();

  public void initialize() {
    channelsView.setShowTransitionFactory(BounceInRightTransition::new);

    FloatingActionButton fab =
        new FloatingActionButton(
            MaterialDesignIcon.INFO.text,
            e -> {
              channelsTableView.getItems().clear();
              final List<Channel> channelList = Kernel.getInstance().getChannelManager().getActiveChannels();
              itens.clear();
              int i = 0;
              for (final Channel channel :channelList ) {
                final ChannelJo jo = new ChannelJo(channel);
                  itens.add(jo);
                System.out.println("#" + (++i) + " - "  + channel.toString() + " #block# " + channel.getRemotePeer().getLatestBlockNumber());
              }
              channelsTableView.setItems(itens);
              System.out.println("Set Channels size - " + itens.size());
            });
    fab.showOn(channelsView);

    channelsView
        .showingProperty()
        .addListener(
            (obs, oldValue, newValue) -> {
              if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(
                    MaterialDesignIcon.MENU.button(
                        e -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Channels");
                appBar
                    .getActionItems()
                    .add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
              }
            });

      channelsTableView.prefHeightProperty().bind(channelsView.heightProperty());
      channelsTableView.prefWidthProperty().bind(channelsView.widthProperty());

      channelsTableView.setRowFactory( tv -> {
          TableRow<ChannelJo> row = new TableRow<>(){
          @Override
          public void updateItem(ChannelJo item, boolean empty) {
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
              ChannelJo data = row.getItem();
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

//      channelsTableView.setOnMousePressed(new EventHandler<MouseEvent>() {
//          @Override
//          public void handle(MouseEvent event) {
//                  if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
//                      ChannelJo jo = channelsTableView.getSelectionModel().getSelectedItem();
//                      System.out.println(jo);
//                  }
//          }
//      });
  }
}
