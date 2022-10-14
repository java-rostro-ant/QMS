/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package qms.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import org.rmj.appdriver.GRider;
import qms.base.ScreenInterface;

/**
 * FXML Controller class
 *
 * @author User
 */
public class LoginController implements Initializable , ScreenInterface {

    private GRider oApp;
     
     
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
    
}
