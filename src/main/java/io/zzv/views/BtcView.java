package io.zzv.views;

import com.gluonhq.charm.glisten.mvc.View;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class BtcView {
    public View getView() {
        try {
            return FXMLLoader.load(BtcView.class.getResource("Btc.fxml"));
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            return new View();
        }
    }
}
