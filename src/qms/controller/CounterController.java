/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package qms.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.naming.ConfigurationException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.StringHelper;
import org.rmj.appdriver.agent.MsgBox;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.constants.EditMode;
import qms.base.LTranDet;
import qms.base.Queue;
import qms.base.ScreenInterface;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CounterController implements Initializable, ScreenInterface {
   
    private double xOffset = 0; 
    private double yOffset = 0;
    private GRider oApp;
    private Queue oTrans;
    private LTranDet oListener;
    private static Properties po_props;
    private int pnEditMode;
    private boolean pbLoaded = false;
    @FXML
    private AnchorPane AnchorParent;
    @FXML
    private Pane btnMin;
    @FXML
    private Pane btnClose;
    @FXML
    private Label DateAndTime, lblNumber, lblCounter;
    @FXML
    private Button btnPrevious, btnNext, btnDone;
            
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // TODO
        getTime();
        
        oListener = new LTranDet() {
            @Override
            public void MasterRetreive(int fnIndex, Object foValue) {
              
                switch(fnIndex){
                    case 1:
                        break;
                }
            }

            @Override
            public void DetailRetreive(int fnRow, int fnIndex, Object foValue) {
              System.out.println("loaded");
            }
        };
        
        
        oTrans = new Queue(oApp, oApp.getBranchCode(), false);
        oTrans.setListener(oListener);
        oTrans.setTranStat(01);
        oTrans.setWithUI(true);
        loadOngoing();
        pbLoaded = true;
        btnPrevious.setOnAction(this::cmdButton_Click);
        btnNext.setOnAction(this::cmdButton_Click);
        btnDone.setOnAction(this::cmdButton_Click);
    }
    
    private Stage getStage(){
	return (Stage) AnchorParent.getScene().getWindow();
    }

    @FXML
    private void handleButtonCloseClick(MouseEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleButtonMinimizeClick(MouseEvent event) {
        Stage stage = (Stage) btnMin.getScene().getWindow();
        stage.setIconified(true);
    }
    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
    
    private void initButton(int fnValue){
        boolean lbShow = (fnValue == EditMode.ADDNEW);
    }
    private void loadOngoing(){
        try {
             if(oTrans.OpenCounter()){ 
                if(oTrans.getItemCount()>0){
                   lblCounter.setText(StringHelper.prepad((String) oTrans.getMaster("sCtrNmber"), 4, '0'));

                   
                }else{
                    lblCounter.setText("");
                }
                if(oTrans.OpenOngoing()){
                    
                    lblNumber.setText(StringHelper.prepad((String) oTrans.getOngoing("sCtrNmber"), 4, '0'));
                } 
             }   
        } catch (SQLException ex) {
            Logger.getLogger(CounterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button)event.getSource()).getId();
        try {
            switch (lsButton){
                case "btnPrevious":
                    if(oTrans.PreviousTransaction()){
                        loadOngoing();
                    }
                    break;
                case "btnNext":
                    if(oTrans.NewTransaction()){
                        loadOngoing();
                        if(!lblNumber.getText().toString().isEmpty()){ 
                            if(oTrans.updateToNotActive((String)oTrans.getMaster("sTransNox"))){
                                
                            }
                        }
                    }
                    break;
                
                case "btnDone":
                    if(!lblCounter.getText().toString().isEmpty()){ 
                        if(oTrans.UpdateToDone((String)oTrans.getMaster("sTransNox"))){
                            loadOngoing();
                        }else{
                              ShowMessageFX.Warning(getStage(), oTrans.getMessage(),"Warning", null);
                        }
                    }
                    
                    break;
                
            }
            
            initButton(pnEditMode);
        } catch (SQLException e) {
            e.printStackTrace();
            ShowMessageFX.Warning(getStage(),e.getMessage(), "Warning", null);
        }
    }   
    private void getTime(){
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {            
        Calendar cal = Calendar.getInstance();
        int second = cal.get(Calendar.SECOND);        
        String temp = "" + second;
        
        Date date = new Date();
        String strTimeFormat = "hh:mm:ss a";
        String strDateFormat = "MMMM dd, yyyy";
        String secondFormat = "ss";
        
        DateFormat timeFormat = new SimpleDateFormat(strTimeFormat);
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        
        String formattedTime= timeFormat.format(date);
        String formattedDate= dateFormat.format(date);
        
        DateAndTime.setText(formattedDate+ "   ||   " + formattedTime);
        
        }),
         new KeyFrame(Duration.seconds(1))
        );
        
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
}
