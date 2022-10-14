/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qms;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.rmj.appdriver.GRider;
import qms.controller.FXMLDocumentController;

public class GriderGui extends Application {
    public final static String pxeMainFormTitle = "Incentives";
    public final static String pxeMainForm = "/qms/view/FXMLDocument.fxml";
    public final static String pxeStageIcon = "images/icon.png";
    public static GRider oApp;
    
    private double xOffset = 0; 
    private double yOffset = 0;
    @Override
    public void start(Stage stage) throws Exception {        
        FXMLLoader view = new FXMLLoader();
        view.setLocation(getClass().getResource(pxeMainForm));
        
        FXMLDocumentController controller = new FXMLDocumentController();
        controller.setGRider(oApp);
        
        view.setController(controller);        
        Parent parent = view.load();

        parent.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public void setGRider(GRider foValue){
        oApp = foValue;
    }
}
