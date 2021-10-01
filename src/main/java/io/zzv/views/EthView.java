package io.zzv.views;

import com.gluonhq.charm.glisten.mvc.View;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class EthView {
    public View getView() {
        try {
            return FXMLLoader.load(EthView.class.getResource("Eth.fxml"));
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            return new View();
        }
    }
}
