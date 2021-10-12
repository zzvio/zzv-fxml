package io.zzv.views;

import java.io.IOException;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXMLLoader;

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
