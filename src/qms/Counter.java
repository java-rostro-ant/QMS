package qms;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.rmj.appdriver.GRider;
import qms.controller.FXMLCounterController;

public class Counter extends Application {
    public final static String pxeMainForm = "/qms/view/FXMLDocument.fxml";
    
    public static GRider oApp;
    
    private double xOffset = 0; 
    private double yOffset = 0;
    
    @Override
    public void start(Stage stage) throws Exception {        
        FXMLLoader view = new FXMLLoader();
        view.setLocation(getClass().getResource(pxeMainForm));
        
        FXMLCounterController controller = new FXMLCounterController();
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
