/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package qms.controller;

import com.gtranslate.Audio;
import com.gtranslate.Language;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import static java.awt.SystemColor.text;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.StringHelper;
import qms.base.LTranDet;
import qms.base.Queue;
import qms.base.ScreenInterface;
import qms.model.TableModel;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DisplayController implements Initializable, ScreenInterface {
    private GRider oApp;
    String readText ="";
    private Queue oTrans;
    private LTranDet oListener;
    private String ctr_number = "";
    private int seconds = 0;
    
    @FXML
    private AnchorPane AnchorParent;
    @FXML
    private VBox vbBody;
    @FXML
    private BorderPane toolbar;
    @FXML
    private GridPane gridNumber;
    @FXML
    private ImageView imgLogo,imgFest,imgMinimize,imgClose;
    @FXML
    private Pane btnMin;
    @FXML
    private Pane btnClose;
    @FXML
    private Label DateAndTime,lblServing, lblServing1, lblCounter,lblTitle,lblTableTitle,lblProceed;
    @FXML
    private TableView tblServings;
    @FXML
    private TableColumn index01,index02;
    
    private final ObservableList<TableModel> data = FXCollections.observableArrayList();
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
            }
        };
        
        oTrans = new Queue(oApp, oApp.getBranchCode(), false);
        oTrans.setListener(oListener);
        oTrans.setTranStat(1);
        oTrans.setWithUI(true);
        ctr_number = System.getProperty("counter.ctr_numer");
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(Integer.parseInt(System.getProperty("display.thread.seconds"))), e -> {
            loadDetail();
            loadOngoing();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        System.out.println("Height: " + screenBounds.getHeight() + " Width: " + screenBounds.getWidth());
        
        AnchorParent.setMaxWidth(screenBounds.getWidth());
        AnchorParent.setMaxHeight(screenBounds.getHeight());
        System.out.println("AnchorPane Size");
        System.out.println("Height: " + AnchorParent.getPrefHeight()+ " Width: " + AnchorParent.getPrefWidth());
        
        if(screenBounds.getHeight()>=720 && screenBounds.getHeight()<=768){
            lblTitle.getStyleClass().add("lbl-title-default");
            lblServing.getStyleClass().add("lbl-serving-default");
            lblServing1.getStyleClass().add("lbl-serving-default-1");
            DateAndTime.getStyleClass().add("lbl-time-default");
            lblTableTitle.getStyleClass().add("lbl-table-title-default");
            if(screenBounds.getHeight()>720 ){
                AnchorParent.getStylesheets().add("/qms/css/StyleSheet_1.css");
            
                imgLogo.setFitWidth(24);
                imgLogo.setFitHeight(24);
                index01.setMinWidth(220);
                index02.setMinWidth(195);
            }else{
                AnchorParent.getStylesheets().add("/qms/css/StyleSheet.css");
            
                imgLogo.setFitWidth(16);
                imgLogo.setFitHeight(16);
                index01.setMinWidth(220);
                index02.setMinWidth(182);
            }
            
            imgFest.setFitWidth(680);
            imgFest.setFitHeight(170);
            vbBody.setSpacing(5);
//            lblNowSerning.getStyleClass().add("lbl-now-serving-deafult");
            gridNumber.setStyle("-fx-margin: 0 20 0 20");
           
        }else if(screenBounds.getHeight()>768){
            lblTitle.getStyleClass().add("lbl-title-large");
            lblServing.getStyleClass().add("lbl-serving-large");
            lblServing1.getStyleClass().add("lbl-serving-large-1");
            DateAndTime.getStyleClass().add("lbl-time-large");
            lblTableTitle.getStyleClass().add("lbl-table-title-large");
            AnchorParent.getStylesheets().add("/qms/css/StyleSheet_2.css");
            imgLogo.setFitWidth(24);
            imgLogo.setFitHeight(24);
            index01.setMinWidth(400);
            index02.setMinWidth(290);
            
            imgFest.setFitWidth(720);
            imgFest.setFitHeight(190);
            
            vbBody.setSpacing(5);
//            lblNowSerning.getStyleClass().add("lbl-now-serving-deafult");
            gridNumber.setStyle("-fx-margin: 0 20 0 20");
        }
    }    
    
    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
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
    private void loadDetail(){
        try {
            
            if(oTrans.OpenOnDispaly()){
                data.clear();
                for (int i = 1; i <= oTrans.getDisplayItemCount(); i++) {
                    String ctr_number = (String)oTrans.getOnDisplay(i,"sCtrNmber");
                    if(!ctr_number.isEmpty()){
                        ctr_number = StringHelper.prepad((String)oTrans.getOnDisplay(i,"sCtrNmber"), 4, '0');
                    }
                    data.add(new TableModel((String)oTrans.getOnDisplay(i,2)
                        , ctr_number));
                }
                initGrid();
            }
        } catch (SQLException ex) {
                Logger.getLogger(DisplayController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    private void loadOngoing(){
        try {
            if(oTrans.OpenOngoing()){
               
                lblCounter.setText("Cashier #"+(String) oTrans.getOngoing("sCtrCodex"));
                lblServing.setText(StringHelper.prepad((String) oTrans.getOngoing("sCtrNmber"), 4, '0'));
                lblServing1.setText(StringHelper.prepad((String) oTrans.getOngoing("sCtrNmber"), 4, '0'));
                if(!oTrans.getOngoing("sCtrNmber").toString().equalsIgnoreCase(System.getProperty("counter.number"))){
                    System.setProperty("counter.number",oTrans.getOngoing("sCtrNmber").toString());
                    String path = "D:/GGC_Java_Systems/audio/door_bell.mp3";  
//
                    //Instantiating Media class  
                    Media media = new Media(new File(path).toURI().toString());  

                    //Instantiating MediaPlayer class   
                    MediaPlayer mediaPlayer = new MediaPlayer(media); 
//                    mediaPlayer.setAutoPlay(true); 
                    mediaPlayer.play();
                }
            }    
        } catch (SQLException ex) {
            Logger.getLogger(CounterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void initGrid(){
        index01.setStyle("-fx-alignment: CENTER;");
        index02.setStyle("-fx-alignment: CENTER;");
        index01.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblServings.setItems(data);
        tblServings.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblServings.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });
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
