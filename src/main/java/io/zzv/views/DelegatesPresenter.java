package io.zzv.views;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class DelegatesPresenter {

    @FXML
    private View delegates;

    @FXML
    private TextField textUrl;
//    @FXML
//    private WebView webView;
//    private WebEngine webEngine;

    @FXML
    private ProgressBar progressBar;

    private void loadUrl(){
//        try {
//            final WebEngine webEngine = webView.getEngine();
//            webEngine.load(textUrl.getText());
//
//        }catch (Exception ex){
//            System.out.println("Url Problem:" + ex.getCause().getMessage());
//            ex.printStackTrace();
//            return;
//        }
        System.out.println("WebView -> " + textUrl.getText());
    }

    public void initialize() {
        delegates.setShowTransitionFactory(BounceInRightTransition::new);

        textUrl.setText("https://google.com");
//        webEngine = webView.getEngine();
//        // Removing right clicks
//        webView.setContextMenuEnabled(false);
//        // Updating progress bar using binding property
//        progressBar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
//        webEngine.getLoadWorker().stateProperty().addListener(
//                (ov, oldState, newState) -> {
//                    if (newState == Worker.State.SUCCEEDED) {
//                        // Hide progress bar when page is ready
//                        progressBar.setVisible(false);
//                    } else {
//                        //Showing progress bar
//                        progressBar.setVisible(true);
//                    }
//                });

        FloatingActionButton fab = new FloatingActionButton(MaterialDesignIcon.INFO.text,
                e -> {
                    loadUrl();
            });
        fab.showOn(delegates);
        
        delegates.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Delegates");
                appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e -> 
                        System.out.println("Favorite")));
            }
        });
    }
}
