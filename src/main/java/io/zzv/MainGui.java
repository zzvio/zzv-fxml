package io.zzv;

import org.semux.cli.SemuxCli;
import org.semux.event.PubSubFactory;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;

import io.zzv.views.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainGui extends MobileApplication {

  public static final String PRIMARY_VIEW = HOME_VIEW;
  public static final String RECEIVE_VIEW = "Receive";
  public static final String SEND_VIEW = "Send";
  public static final String TRANSACTIONS_VIEW = "Transactions";
  public static final String DELEGATES_VIEW = "Delegates";
  public static final String CHANNELS_VIEW = "Channels";

  private static SemuxCli chain = null;

  public static void main(String[] args) {
    chain = new SemuxCli();
    SemuxCli.main(args, chain);
    PubSubFactory.getDefault().start();
    SemuxCli.registerShutdownHook("pubsub-default", () -> PubSubFactory.getDefault().stop());
    launch(args);
    chain.stop();
    System.exit(0);
  }

  @Override
  public void init() {

    addViewFactory(PRIMARY_VIEW, () -> new HomeView().getView());
    addViewFactory(RECEIVE_VIEW, () -> new ReceiveView().getView());
    addViewFactory(SEND_VIEW, () -> new SendView().getView());
    addViewFactory(TRANSACTIONS_VIEW, () -> new TransactionsView().getView());
    addViewFactory(DELEGATES_VIEW, () -> new DelegatesView().getView());
    addViewFactory(CHANNELS_VIEW, () -> new ChannelsView().getView());

    DrawerManager.buildDrawer(this);
  }

  @Override
  public void postInit(Scene scene) {
    Swatch.BLUE.assignTo(scene);

    scene.getStylesheets().add(MainGui.class.getResource("style.css").toExternalForm());
    ((Stage) scene.getWindow())
        .getIcons()
        .add(new Image(MainGui.class.getResourceAsStream("/icon.png")));
  }
}
