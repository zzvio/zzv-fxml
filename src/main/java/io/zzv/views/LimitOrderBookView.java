package io.zzv.views;

import com.gluonhq.charm.glisten.mvc.View;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class LimitOrderBookView {
    public View getView() {
        try {
            return FXMLLoader.load(HomeView.class.getResource("LimitOrderBook.fxml"));
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            return new View();
        }
    }
}
