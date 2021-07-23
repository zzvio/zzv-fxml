package io.zzv.views;

import java.io.IOException;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;

public class DelegatesView {

  public View getView() {
    try {
      View view = FXMLLoader.load(DelegatesView.class.getResource("Delegates.fxml"));
      return view;
    } catch (LoadException le){
      System.out.println("LoadException: " + le);
      System.out.println("LoadException Cause: " + le.getCause().getMessage());
      return new View();
    } catch (IOException e) {
      System.out.println("IOException: " + e);
      return new View();
    }
  }
}
