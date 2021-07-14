package io.zzv.views;

import java.io.IOException;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXMLLoader;

public class DebugView {

  public View getView() {
    try {
      View view = FXMLLoader.load(DebugView.class.getResource("Debug.fxml"));
      return view;
    } catch (IOException e) {
      System.out.println("IOException: " + e);
      return new View();
    }
  }
}
