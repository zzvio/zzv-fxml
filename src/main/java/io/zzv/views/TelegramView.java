package io.zzv.views;

import com.gluonhq.charm.glisten.mvc.View;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

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
