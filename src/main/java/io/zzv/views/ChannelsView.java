package io.zzv.views;

import java.io.IOException;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXMLLoader;

public class ChannelsView {

  public View getView() {
    try {
      View view = FXMLLoader.load(ChannelsView.class.getResource("Channels.fxml"));
      return view;
    } catch (IOException e) {
      System.out.println("IOException: " + e);
      return new View();
    }
  }
}
