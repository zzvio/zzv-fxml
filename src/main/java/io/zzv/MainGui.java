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
    public static final String PLUGINS_VIEW = "Plugins";
    public static final String LIMITORDERBOOK_VIEW = "LimitOrderBook";
    public static final String TELEGRAM_VIEW = "Telegram";
    public static final String BTC_VIEW = "Btc";
    public static final String ETH_VIEW = "Eth";
    public static final String DEBUG_VIEW = "Debug";

    private static SemuxCli chain = null;

    public static void main(String[] args) {

        chain = new SemuxCli();
        try {
            SemuxCli.main(args, chain);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PubSubFactory.getDefault().start();
        SemuxCli.registerShutdownHook("pubsub-default", () -> PubSubFactory.getDefault().stop());
        launch(args);
        try {
            chain.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

//    public static void main(String[] args) {
//
//        MainGui.MyRunnable myRunnable = new MainGui.MyRunnable(args);
//        Thread t = new Thread(myRunnable);
//        t.start();
//        launch(args);
//        myRunnable.stop();
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }
//        System.exit(0);
//    }

    @Override
    public void init() {

        addViewFactory(PRIMARY_VIEW, () -> new HomeView().getView());
        addViewFactory(RECEIVE_VIEW, () -> new ReceiveView().getView());
        addViewFactory(SEND_VIEW, () -> new SendView().getView());
        addViewFactory(TRANSACTIONS_VIEW, () -> new TransactionsView().getView());
        addViewFactory(DELEGATES_VIEW, () -> new DelegatesView().getView());
        addViewFactory(CHANNELS_VIEW, () -> new ChannelsView().getView());
        addViewFactory(PLUGINS_VIEW, () -> new PluginsView().getView());
        addViewFactory(LIMITORDERBOOK_VIEW, () -> new LimitOrderBookView().getView());
        addViewFactory(TELEGRAM_VIEW, () -> new TelegramView().getView());
        addViewFactory(BTC_VIEW, () -> new BtcView().getView());
        addViewFactory(ETH_VIEW, () -> new EthView().getView());
        addViewFactory(DEBUG_VIEW, () -> new DebugView().getView());
        DrawerManager.initDrawer(this);
//        DrawerManager.homeDrawer();
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.BLUE.assignTo(scene);

        scene.getStylesheets().add(MainGui.class.getResource("style.css").toExternalForm());
        ((Stage) scene.getWindow())
                .getIcons()
                .add(new Image(MainGui.class.getResourceAsStream("/icon.png")));
    }

    private static class MyRunnable implements Runnable {

        private final SemuxCli chain;
        private final String[] args;

        public MyRunnable(String[] args) {
            this.args = args;
            chain = new SemuxCli();
        }

        public void run() {
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println("STARTING KERNEL!!!");
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            SemuxCli.main(args, chain);
            PubSubFactory.getDefault().start();
            SemuxCli.registerShutdownHook("pubsub-default", () -> PubSubFactory.getDefault().stop());
        }

        void stop() {
            chain.stop();
        }
    }
}
