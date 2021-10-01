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

import io.zzv.views.BtcView;
import javafx.scene.image.Image;

public class DrawerManager {
  static NavigationDrawer drawer;
  static final Item homeItem = new ViewItem("Home", MaterialDesignIcon.HOME.graphic(), PRIMARY_VIEW, ViewStackPolicy.SKIP);
  static final Item debugItem = new ViewItem("Debug", MaterialDesignIcon.DASHBOARD.graphic(), DEBUG_VIEW);

  public static void initDrawer(MobileApplication app){
    drawer = app.getDrawer();
  }

  public static void pluginDrawer(){
    final Item lobItem = new ViewItem("Limit Order Book", MaterialDesignIcon.DASHBOARD.graphic(), LIMITORDERBOOK_VIEW);
    final Item telegramItem = new ViewItem("Telegram", MaterialDesignIcon.DASHBOARD.graphic(), TELEGRAM_VIEW);
    final Item btcItem = new ViewItem("BTC", MaterialDesignIcon.DASHBOARD.graphic(), BTC_VIEW);
    final Item ethItem = new ViewItem("ETH", MaterialDesignIcon.DASHBOARD.graphic(), ETH_VIEW);
    drawer.getItems().clear();
    drawer.getItems().addAll(homeItem, lobItem, telegramItem, btcItem, ethItem, debugItem);
    addQuitItem();
  }

  public static void homeDrawer() {


    NavigationDrawer.Header header =
        new NavigationDrawer.Header(
            "Block-chain Mobile",
            "FX Discovery Viewer",
            new Avatar(21, new Image(DrawerManager.class.getResourceAsStream("/icon.png"))));
    drawer.setHeader(header);

    final Item receiveItem = new ViewItem("Receive", MaterialDesignIcon.DASHBOARD.graphic(), RECEIVE_VIEW);
    final Item sendItem = new ViewItem("Send", MaterialDesignIcon.DASHBOARD.graphic(), SEND_VIEW);
    final Item transactionsItem = new ViewItem("Transactions", MaterialDesignIcon.DASHBOARD.graphic(), TRANSACTIONS_VIEW);
    final Item delegatesItem = new ViewItem("Delegates", MaterialDesignIcon.DASHBOARD.graphic(), DELEGATES_VIEW);
    final Item channelsItem = new ViewItem("Channels", MaterialDesignIcon.DASHBOARD.graphic(), CHANNELS_VIEW);
    final Item pluginsItem = new ViewItem("Plugins", MaterialDesignIcon.DASHBOARD.graphic(), PLUGINS_VIEW);

    drawer.getItems().clear();
    drawer.getItems().addAll(homeItem, receiveItem, sendItem, transactionsItem, delegatesItem, channelsItem, pluginsItem, debugItem);
    addQuitItem();
  }
  static void addQuitItem(){
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
