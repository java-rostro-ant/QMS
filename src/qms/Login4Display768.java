package qms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javafx.application.Application;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;

public class Login4Display768 {
    public static void main(String [] args){      
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "D:/GGC_Java_Systems";
        }
        else{
            path = "/srv/mac/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        
        GRider oApp = new GRider();
        
        if (!oApp.loadEnv("gRider")) {
            System.err.println(oApp.getErrMsg());
            System.exit(1);
        }
        
        if (!oApp.logUser("gRider", "M001111122")) {
            System.err.println(oApp.getErrMsg());
            System.exit(1);
        }   
        
        loadProperties();
        
        Display768 instance = new Display768();
        instance.setGRider(oApp);
        
        try {
            String lsSQL = "SELECT * FROM Queueing_Info ORDER BY sTransNox DESC LIMIT 1";
            ResultSet loRS = oApp.executeQuery(lsSQL);

            if (loRS.next()){
                if (!loRS.getString("dTransact").equals(SQLUtil.dateFormat(oApp.getServerDate(), SQLUtil.FORMAT_SHORT_DATE))){
                    lsSQL = "DELETE FROM Queueing_Ongoing";
                    oApp.executeQuery(lsSQL, "Queueing_Ongoing", oApp.getBranchCode(), "");
                    
                    lsSQL = "UPDATE Queueing_Info SET cTranStat = '2'" +
                            " WHERE cTranStat <> '2'" +
                                " AND dTransact = " + SQLUtil.toSQL(loRS.getString("dTransact"));
                    oApp.executeQuery(lsSQL, "Queueing_Info", oApp.getBranchCode(), "");
                }
            }
        } catch (SQLException e) {
        }
        
        Application.launch(instance.getClass());
    }
    
    private static boolean loadProperties(){
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream(System.getProperty("sys.default.path.config") + "/config/CounterNo.properties"));

            System.setProperty("counter.id", po_props.getProperty("counter.id"));
            System.setProperty("display.thread.seconds", po_props.getProperty("display.thread.seconds"));
            System.setProperty("counter.number", po_props.getProperty("counter.number"));
            
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
      
        }
    }
   
}