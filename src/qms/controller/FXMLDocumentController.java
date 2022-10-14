/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package qms.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.rmj.appdriver.GRider;
import qms.base.ScreenInterface;
import qms.controller.CounterController;
import qms.controller.LoginController;
import qms.controller.OnDisplayController;

/**
 *
 * @author User
 */
public class FXMLDocumentController implements Initializable, ScreenInterface {

     private GRider oApp;
    private String reportName;
    
    @FXML
    private StackPane workingSpace;
    
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setScene(loadAnimate("/qms/view/Counter.fxml"));
        
    }    
    
    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
    }

    private AnchorPane loadAnimate(String fsFormName){
        ScreenInterface fxObj = getController(fsFormName);
        fxObj.setGRider(oApp);
       
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(fsFormName));
        fxmlLoader.setController(fxObj);    
        
        AnchorPane root;
        try {
            root = (AnchorPane) fxmlLoader.load();
            FadeTransition ft = new FadeTransition(Duration.millis(1500));
            ft.setNode(root);
            ft.setFromValue(1);
            ft.setToValue(1);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft.play();

            return root;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }
    
    private ScreenInterface getController(String fsValue){
        switch (fsValue){
            case "view/Login.fxml":
                return new LoginController();
            case "/qms/view/Counter.fxml":
                return new CounterController();
            case "/qms/view/OnDisplay.fxml":
                return new OnDisplayController();
            default:
                return null;
        }
    }
 
    private void setScene(AnchorPane foPane){
        workingSpace.getChildren().clear();
        workingSpace.getChildren().add(foPane);
    }



}
