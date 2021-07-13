package io.zzv.views;

import java.io.IOException;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXMLLoader;

public class HomeView {

  public View getView() {
    try {
      return FXMLLoader.load(HomeView.class.getResource("Home.fxml"));
    } catch (IOException e) {
      System.out.println("IOException: " + e);
      return new View();
    }
  }
}
