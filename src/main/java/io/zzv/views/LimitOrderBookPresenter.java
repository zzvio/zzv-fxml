package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import io.zzv.DrawerManager;
import io.zzv.plugins.LimitOrderBook.Loader;
import io.zzv.plugins.LimitOrderBook.Order;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LimitOrderBookPresenter {
    @FXML
    private View lobView;
    @FXML
    private TextArea response;
    Loader loader;
    private static LimitOrderBookPresenter limitOrderBookPresenter;
    public void initialize() {
        lobView.setShowTransitionFactory(BounceInRightTransition::new);

        lobView
        .showingProperty()
        .addListener(
        (obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(
                                e -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Limit Order Book");
                DrawerManager.pluginDrawer();
                appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
            }
        });
        loader = new Loader();
        Thread thread = new Thread(loader);
        thread.start();
        limitOrderBookPresenter = this;
    }

    public void NewOrder(){
        int clientID = 1111; // Mock clientOrderID

        byte bookType = (byte) 2; // Kilo book
        int tokenID = 2;
        int benchmarkID = 1;
        Order order = new Order(tokenID, benchmarkID, clientID, (byte) 140, (byte) 2, bookType, (byte) 1, 1, 0, 103, 0);
        Loader.run(order);;
    }

    public void CancelOrder(){
        int clientID = 1111; // Mock clientOrderID
        int orderID = Loader.orderIDMap.get(clientID);

        byte bookType = (byte) 2; // Kilo book
        int tokenID = 2;
        int benchmarkID = 1;
        Order order = new Order(tokenID, benchmarkID, orderID, (byte) 140, (byte) 3, bookType, (byte) 1, 1, 0, 103, 0);
        Loader.run(order);
    }

    public static void Response(String msg){
        limitOrderBookPresenter.response.setText(msg);
    }

}
