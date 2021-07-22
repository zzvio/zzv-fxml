package io.zzv.views;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChannelsPresenter {

  @FXML private View channelsView;
  @FXML private TableView<ChannelJo> channelsTableView;

  public void initialize() {
    channelsView.setShowTransitionFactory(BounceInRightTransition::new);

    FloatingActionButton fab =
        new FloatingActionButton(
            MaterialDesignIcon.INFO.text,
            e -> {
              channelsTableView.getItems().clear();
              final List<Channel> channelList = Kernel.getInstance().getChannelManager().getActiveChannels();
              final ObservableList<ChannelJo> list = FXCollections.observableArrayList();
              int i = 0;
              for (final Channel channel :channelList ) {
                final ChannelJo jo = new ChannelJo(channel);
                list.add(jo);
                System.out.println("#" + (++i) + " - "  + channel.toString() + " #block# " + channel.getRemotePeer().getLatestBlockNumber());
              }
              channelsTableView.setItems(list);
              System.out.println("Set Channels size - " + list.size());
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

      channelsTableView.setRowFactory( tv -> {
          TableRow<ChannelJo> row = new TableRow<>();
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
