package io.zzv.views;

import java.io.IOException;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXMLLoader;

public class ReceiveView {

  public View getView() {
    try {
      View view = FXMLLoader.load(ReceiveView.class.getResource("Receive.fxml"));
      return view;
    } catch (IOException e) {
      System.out.println("IOException: " + e);
      return new View();
    }
  }
}
