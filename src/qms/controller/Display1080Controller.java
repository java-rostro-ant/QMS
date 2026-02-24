package qms.controller;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.StringHelper;
import qms.base.LTranDet;
import qms.base.Queue;
import qms.base.ScreenInterface;
import qms.model.TableModel;

public class Display1080Controller implements Initializable, ScreenInterface {
    private GRider oApp;
    private Queue oTrans;
    private LTranDet oListener;
    private String ctr_number;
    
    @FXML
    private Label DateAndTime;
    @FXML
    private Label lblServing;
    @FXML
    private Label lblCounter;
    @FXML
    private TableView tblServings;
    @FXML
    private TableColumn index01;
    @FXML
    private TableColumn index02;
    
    private final ObservableList<TableModel> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
        
        lblCounter.setText("");
        lblServing.setText("");
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(Integer.parseInt(System.getProperty("display.thread.seconds"))), e -> {
            loadDetail();
            loadOngoing();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }    

    @Override
    public void setGRider(GRider foValue) {
        oApp = foValue;
    }
    
    @FXML
    private void closeForm(MouseEvent event) {
        Stage stage = (Stage) DateAndTime.getScene().getWindow();
        stage.close();
    }
    
    private Stage getStage(){
	return (Stage) DateAndTime.getScene().getWindow();
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
                Logger.getLogger(DisplayController1.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void loadOngoing(){
        try {
            if(oTrans.OpenOngoing()){
                lblCounter.setText((String) oTrans.getOngoing("sCtrCodex"));
                lblServing.setText(StringHelper.prepad((String) oTrans.getOngoing("sCtrNmber"), 4, '0'));
                if(!oTrans.getOngoing("sCtrNmber").toString().equalsIgnoreCase(System.getProperty("counter.number"))){
                    System.setProperty("counter.number",oTrans.getOngoing("sCtrNmber").toString());
                    String path = "D:/GGC_Java_Systems/audio/door_bell.mp3";  

                    //Instantiating Media class  
                    Media media = new Media(new File(path).toURI().toString());  

                    //Instantiating MediaPlayer class   
                    MediaPlayer mediaPlayer = new MediaPlayer(media); 
                    mediaPlayer.play();
                }
            }    
        } catch (SQLException ex) {
            Logger.getLogger(CounterController.class.getName()).log(Level.SEVERE, null, ex);
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
