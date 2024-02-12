package qms.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.rmj.appdriver.GRider;
import qms.base.ScreenInterface;

/**
 *
 * @author User
 */
public class FXMLDisplayController implements Initializable, ScreenInterface {
    private GRider oApp;
    
    @FXML
    private StackPane workingSpace;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        if(screenBounds.getHeight()>=720 && screenBounds.getHeight()<=768){
            setScene(loadAnimate("/qms/view/OnDisplay1366.fxml"));   
        }else{
            setScene(loadAnimate("/qms/view/OnDisplay_1.fxml"));   
        }
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
            case "/qms/view/OnDisplay_1.fxml":
                return new DisplayController1();
            case "/qms/view/OnDisplay1366.fxml":
                return new DisplayController1();
            case "/qms/view/OnDisplay.fxml":
                return new DisplayController();
            default:
                return null;
        }
    }
 
    private void setScene(AnchorPane foPane){
        workingSpace.getChildren().clear();
        workingSpace.getChildren().add(foPane);
    }
}
