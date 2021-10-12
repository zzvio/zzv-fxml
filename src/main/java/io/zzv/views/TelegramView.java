package io.zzv.views;

import java.io.IOException;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXMLLoader;

public class TelegramView {
    public View getView() {
        try {
            return FXMLLoader.load(TelegramView.class.getResource("Telegram.fxml"));
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            return new View();
        }
    }
}
