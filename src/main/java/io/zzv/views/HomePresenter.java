package io.zzv.views;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import io.zzv.debug.KernelFunc;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomePresenter {

  @FXML private View home;

  @FXML private Label label;

  @FXML private Label lbl_jsl;
  @FXML private Label lbl_startLabel;
  @FXML private Label lbl_stopLabel;

  /** A GraalVM engine shared between multiple JavaScript contexts. */
  private final Engine sharedEngine = Engine.newBuilder().build();

  public void initialize() {
    home.showingProperty()
        .addListener(
            (obs, oldValue, newValue) -> {
              if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(
                    MaterialDesignIcon.MENU.button(
                        e -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Home");
                appBar
                    .getActionItems()
                    .add(MaterialDesignIcon.SEARCH.button(e -> System.out.println("Search")));
              }
            });
  }

  @FXML
  void buttonClick() {
    label.setText("Hello JavaFX Universe!");
  }

  @FXML
  void buttonJsClick() {
    System.out.println("Hello before polyglot world Java!");
    try (Context context = Context.create()) {

      context.eval("js", "print('Hello from polyglot world JavaScript!');");
      //        context.eval("ruby", "puts 'Hello polyglot world Ruby!'");
      //        context.eval("R", "print('Hello polyglot world R!');");
      //        context.eval("python", "print('Hello polyglot world Python!');");
    } catch (Exception ex) {
      lbl_jsl.setText("Error - " + ex.getMessage());
      return;
    }
    lbl_jsl.setText("Hello after plyglot world Java!!!");
  }

  @FXML
  void buttonStartClick() {
    try {

      boolean bool = KernelFunc.StartKernel.get();
      if (bool) {
        lbl_startLabel.setText("Started");
      } else {
        lbl_startLabel.setText("Not Started");
      }
    } catch (Exception e) {
      lbl_startLabel.setText(e.getMessage());
      System.out.println(e.getMessage());
    }
  }

  @FXML
  void buttonStopClick() {
    try {
      boolean bool = KernelFunc.StopKernel.get();
      if (bool) {
        lbl_stopLabel.setText("Stopped");
      } else {
        lbl_stopLabel.setText("Not Stopped");
      }
    } catch (Exception e) {
      lbl_stopLabel.setText(e.getMessage());
      System.out.println(e.getMessage());
    }
  }
}
