package io.zzv;

import static io.zzv.MainGui.*;

import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.control.NavigationDrawer.ViewItem;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.scene.image.Image;

public class DrawerManager {

  public static void buildDrawer(MobileApplication app) {
    NavigationDrawer drawer = app.getDrawer();

    NavigationDrawer.Header header =
        new NavigationDrawer.Header(
            "Block-chain Mobile",
            "FX Discovery Viewer",
            new Avatar(21, new Image(DrawerManager.class.getResourceAsStream("/icon.png"))));
    drawer.setHeader(header);

    final Item homeItem =
        new ViewItem(
            "Home", MaterialDesignIcon.HOME.graphic(), PRIMARY_VIEW, ViewStackPolicy.SKIP);
    final Item receiveItem = new ViewItem("Receive", MaterialDesignIcon.DASHBOARD.graphic(), RECEIVE_VIEW);
    final Item sendItem = new ViewItem("Send", MaterialDesignIcon.DASHBOARD.graphic(), SEND_VIEW);
    final Item transactionsItem = new ViewItem("Transactions", MaterialDesignIcon.DASHBOARD.graphic(), TRANSACTIONS_VIEW);
    final Item delegatesItem = new ViewItem("Delegates", MaterialDesignIcon.DASHBOARD.graphic(), DELEGATES_VIEW);
    final Item channelsItem = new ViewItem("Channels", MaterialDesignIcon.DASHBOARD.graphic(), CHANNELS_VIEW);
    final Item debugItem = new ViewItem("Debug", MaterialDesignIcon.DASHBOARD.graphic(), DEBUG_VIEW);

    drawer.getItems().addAll(homeItem, receiveItem, sendItem, transactionsItem, delegatesItem, channelsItem, debugItem);

    if (Platform.isDesktop()) {
      final Item quitItem = new Item("Quit", MaterialDesignIcon.EXIT_TO_APP.graphic());
      quitItem
          .selectedProperty()
          .addListener(
              (obs, ov, nv) -> {
                if (nv) {
                  Services.get(LifecycleService.class).ifPresent(LifecycleService::shutdown);
                }
              });
      drawer.getItems().add(quitItem);
    }
  }
}
