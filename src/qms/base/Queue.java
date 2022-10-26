/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package qms.base;

import com.sun.rowset.CachedRowSetImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.constants.EditMode;

/**
 *
 * @author User
 */
public class Queue {
    
    private final String DEBUG_MODE = "app.debug.mode";
    private final String REQUIRE_CSS = "app.require.css.approval";
    private final String REQUIRE_CM = "app.require.cm.approval";
    private final String REQUIRE_BANK_ON_APPROVAL = "app.require.bank.on.approval";
    
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    private int p_nEditMode;
    private int p_nTranStat;

    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oMaster;
    private CachedRowSet p_oDetail;
    private CachedRowSet p_oDisplay;
    private CachedRowSet p_oCounter;
    private CachedRowSet p_oOngoing;
    
    private LTranDet p_oListener;
    private String MASTER_TABLE = "Queueing_Info";
    private String COUNTER_TABLE = "Queueing_Counter";
    private String ONGOING_TABLE = "Queueing_Ongoing";
    
    public Queue(GRider foApp, String fsBranchCd, boolean fbWithParent){        
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
        
        p_nTranStat = 0;
        p_nEditMode = EditMode.UNKNOWN;
    }
    
    public void setTranStat(int fnValue){
        p_nTranStat = fnValue;
    }
   
    public void setListener(LTranDet foValue){
        p_oListener = foValue;
    }
    
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;
    }
    
    public int getEditMode(){
        return p_nEditMode;
    }
    
    public String getMessage(){
        return p_sMessage;
    }
    public int getItemCount() throws SQLException{
        p_oMaster.last();
        return p_oMaster.getRow();
    }
//    
     public Object getMaster(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oMaster.first();
        return p_oMaster.getObject(fnIndex);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(getColumnIndex(p_oMaster, fsIndex));
    }
    public void setMaster(int fnIndex, Object foValue) throws SQLException{
        p_oMaster.updateString(fnIndex, (String) foValue);
        p_oMaster.updateRow();
    }
//    
    
    public int getDisplayItemCount() throws SQLException{
        p_oDisplay.last();
        return p_oDisplay.getRow();
    }
//    
    public Object getOnDisplay(int fnRow,int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        if (getDisplayItemCount() == 0 || fnRow > getDisplayItemCount()) return null;
        
        p_oDisplay.absolute(fnRow);
        return p_oDisplay.getObject(fnIndex);
    }
    
    public Object getOnDisplay(int fnRow,String fsIndex) throws SQLException{
        return getOnDisplay(fnRow,getColumnIndex(p_oDisplay, fsIndex));
    }
    
    public void setOnDisplay(int fnIndex, Object foValue) throws SQLException{
        p_oDisplay.updateString(fnIndex, (String) foValue);
        p_oDisplay.updateRow();
    }
//    
    public Object getCounter(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oCounter.first();
        return p_oCounter.getObject(fnIndex);
    }
    
    public Object getCounter(String fsIndex) throws SQLException{
        return getCounter(getColumnIndex(p_oCounter, fsIndex));
    }
    
    public Object getOngoing(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oOngoing.first();
        return p_oOngoing.getObject(fnIndex);
    }
    
    public Object getOngoing(String fsIndex) throws SQLException{
        return getOngoing(getColumnIndex(p_oOngoing, fsIndex));
    }
     public void setOngoing(int fnIndex, Object foValue) throws SQLException{
        p_oOngoing.updateString(fnIndex, (String) foValue);
        p_oOngoing.updateRow();
    }
     
    public boolean PreviousTransaction()throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
         
        p_sMessage = "";
        try {
            
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            String lsStat = String.valueOf(p_nTranStat);
            String lsCondition = " cTranStat = '0'";
        
            lsCondition = lsCondition + 
                            " AND sCtrCodex = " + SQLUtil.toSQL((String)getCounter("sCtrCodex")) +
                            " AND sCtrNmber < " + SQLUtil.toSQL(p_oMaster.getString("sCtrNmber"));
            
            lsSQL = getSQ_Master() + lsCondition + "  ORDER BY sTransNox DESC LIMIT 1";
          
            //open master
            loRS = p_oApp.executeQuery(lsSQL);
            String transNo = "";
            while(loRS.next()){
                transNo = loRS.getString("sTransNox");
            }
            
            if(!transNo.isEmpty()){
                return updateToNotActive(transNo) 
                    && updateToActive(transNo);
            }
       } catch (SQLException ex) {
            Logger.getLogger(Queue.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    public boolean NewTransaction() throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        createOngoing();
        String lsSQL;
        p_sMessage = "";
        
        //check muna kung merong nilampasan na mas mataas ang ticket #
        try {
            ResultSet loRS;
            String lsCondition = " cTranStat = '0'";
        
            lsCondition = lsCondition + 
                            " AND sCtrCodex = " + SQLUtil.toSQL((String)getCounter("sCtrCodex")) +
                            " AND sCtrNmber > " + SQLUtil.toSQL(p_oMaster.getString("sCtrNmber"));
            
            lsSQL = getSQ_Master() + lsCondition + "  ORDER BY sTransNox ASC LIMIT 1";
          
            //open master
            loRS = p_oApp.executeQuery(lsSQL);
            String transNo = "";
            while(loRS.next()){
                transNo = loRS.getString("sTransNox");
            }
            
            if(!transNo.isEmpty()){
                return updateToNotActive(transNo) 
                    && updateToActive(transNo);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        if(OpenOngoing()){
            String lsCtrCodex = System.getProperty("counter.id");
            int lnCtr = Integer.parseInt(p_oOngoing.getString("sCtrNmber"));
            
            //deleteOngoing();
            return insertOngoing(lsCtrCodex,String.valueOf(lnCtr + 1));
        }else{
            String lsCtrCodex = System.getProperty("counter.id");
            return insertOngoing(lsCtrCodex, "1");
        }
    }
    private String getTimeSartStop(){
        
        Date date = new Date();
        String strTimeFormat = "hh:mm:ss";
        DateFormat timeFormat = new SimpleDateFormat(strTimeFormat);
        String formattedTime= timeFormat.format(date);
        return formattedTime;
    }
    private boolean insertInfo(String lsCodex, String lsNumber){
        String lsSQL ;
        
        
        String trasNo = SQLUtil.toSQL(MiscUtil.getNextCode(MASTER_TABLE, "sTransNox", true, p_oApp.getConnection(), p_sBranchCd));
        lsSQL = "INSERT INTO Queueing_Info SET "+
                "  sTransNox = " + trasNo +
                ", cTranStat = 1" +
                ", dTransact = " + SQLUtil.toSQL(p_oApp.getServerDate()) +
                ", dTimeStrt = " + SQLUtil.toSQL(getTimeSartStop()) +
                ", sCtrNmber = " + SQLUtil.toSQL(lsNumber) +
                ", sCtrCodex = " + SQLUtil.toSQL(lsCodex)+
                ", sModified = " + SQLUtil.toSQL(p_oApp.getUserID()) +
                ", dModified = " + SQLUtil.toSQL(p_oApp.getServerDate());
       
        if (!lsSQL.isEmpty()){
        if (!p_bWithParent) p_oApp.beginTrans();

        if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
            if (!p_bWithParent) p_oApp.rollbackTrans();
            p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
            return false;
        }

        if (!p_bWithParent) p_oApp.commitTrans();
            System.out.println("Record successfully save");
            p_nEditMode = EditMode.ADDNEW;
                return true;
        } else{
            p_sMessage = "No record to save.";
                p_nEditMode = EditMode.ADDNEW;
                return false;
        }
        
            
        
    }
    
    public boolean updateToNotActive(String lsTransNo){
        String lsSQL;
        lsSQL = "UPDATE Queueing_Info SET "+
                " cTranStat = 0" +
                " WHERE sCtrCodex = " + SQLUtil.toSQL(System.getProperty("counter.id")) +
                    " AND cTranStat <> '2'" +
                    " AND sTransNox <> " +  SQLUtil.toSQL(lsTransNo);

       if (!lsSQL.isEmpty()){
           if (!p_bWithParent) p_oApp.beginTrans();

           if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
               if (!p_bWithParent) p_oApp.rollbackTrans();
               p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
               return false;
           }

           if (!p_bWithParent) p_oApp.commitTrans();
               System.out.println("Record updated to not active.");
               p_nEditMode = EditMode.ADDNEW;
               return true;
       } else{
           p_sMessage = "No record to save.";
               p_nEditMode = EditMode.ADDNEW;
               return false;
       }
    }
    private boolean updateToActive(String lsTransNo){
        String lsSQL = "UPDATE Queueing_Info SET "+
                    " cTranStat = 1" +
                    " WHERE sCtrCodex = " + SQLUtil.toSQL(System.getProperty("counter.id")) +
                    " AND sTransNox = " +  SQLUtil.toSQL(lsTransNo);
//            lsSQL = MiscUtil.rowset2SQL(p_oOngoing, ONGOING_TABLE, "");
            
        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();

            if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                return false;
            }

            if (!p_bWithParent) p_oApp.commitTrans();
                System.out.println("Record set to active.");
                p_nEditMode = EditMode.ADDNEW;
                return true;
        } else{
            p_sMessage = "No record to save.";
                p_nEditMode = EditMode.ADDNEW;
                return false;
        }
    }
    public boolean UpdateToDone(String lsTransNo){
        
        
      
        String lsSQL = "UPDATE Queueing_Info SET "+
                    " cTranStat = 2" +
                    ", dTimeStop = " + SQLUtil.toSQL(getTimeSartStop()) +
                    " WHERE sCtrCodex = " + SQLUtil.toSQL(System.getProperty("counter.id")) +
                    " AND sTransNox = " + SQLUtil.toSQL(lsTransNo);
//            lsSQL = MiscUtil.rowset2SQL(p_oOngoing, ONGOING_TABLE, "");
            
        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();

            if (p_oApp.executeQuery(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                
                return false;
            }

            if (!p_bWithParent) p_oApp.commitTrans();
                System.out.println("Record successfully done");
                updatePrevious();
                p_nEditMode = EditMode.ADDNEW;
                return true;
        } else{
            p_sMessage = "No record to save.";
                p_nEditMode = EditMode.ADDNEW;
                return false;
        }
    }
    private void updatePrevious(){
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();
            String lsStat = String.valueOf(p_nTranStat);
            String lsCondition = " cTranStat = '0'";
                lsCondition = lsCondition + " AND sCtrCodex = " + SQLUtil.toSQL((String)getCounter("sCtrCodex"));

            lsSQL = getSQ_Master() + lsCondition + "  ORDER BY sTransNox DESC LIMIT 1";

            //open master
            loRS = p_oApp.executeQuery(lsSQL);
            String transNo = "";
            if(loRS != null){
                while(loRS.next()){
                    
                    updateToActive(loRS.getString("sTransNox"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Queue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    private boolean insertOngoing(String lsCodex, String lsNumber) throws SQLException{
        //check first if it reached the max ticket
        String lsSQL = "SELECT * FROM Queueing_Ongoing" +
                        " ORDER BY sCtrNmber DESC LIMIT 1";
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (loRS.next()){
            System.out.println(Integer.parseInt(loRS.getString("sCtrNmber")));
            System.out.println(Integer.parseInt(System.getProperty("counter.max.number")));
            
            if (Integer.parseInt(loRS.getString("sCtrNmber")) > 
                Integer.parseInt(System.getProperty("counter.max.number"))){
                lsNumber = "1";
            }
        }
        
        deleteOngoing();
        lsSQL = "INSERT INTO Queueing_Ongoing SET "+
                "  sCtrCodex = " +SQLUtil.toSQL(lsCodex) + 
                ", sCtrNmber = " +SQLUtil.toSQL(lsNumber);
            
        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();

            if (p_oApp.executeQuery(lsSQL, ONGOING_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();
                return false;
            }

            if (!p_bWithParent) p_oApp.commitTrans();
            System.out.println("Record successfully save to ongoing");
            p_nEditMode = EditMode.ADDNEW;
            
            return insertInfo(lsCodex, lsNumber);
        } else{
            p_sMessage = "No record to save.";
                p_nEditMode = EditMode.ADDNEW;
                return false;
        }
    }
    private void deleteOngoing(){
        String lsSQL = "DELETE FROM Queueing_Ongoing";
            
        if (!lsSQL.isEmpty()){
            if (!p_bWithParent) p_oApp.beginTrans();

            if (p_oApp.executeQuery(lsSQL, ONGOING_TABLE, p_sBranchCd, "") <= 0){
                if (!p_bWithParent) p_oApp.rollbackTrans();
                p_sMessage = p_oApp.getMessage() + ";" + p_oApp.getErrMsg();

            }

            if (!p_bWithParent) p_oApp.commitTrans();
            System.out.println("Record successfully delete.");
        } else{
            p_sMessage = "No record to delete.";
        }
    }
    public boolean OpenCounter(){
         if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            lsSQL = getSQ_Counter() + " WHERE sCtrCodex = " + SQLUtil.toSQL(System.getProperty("counter.id"));
        
            //open master
            loRS = p_oApp.executeQuery(lsSQL);
            p_oCounter = factory.createCachedRowSet();
            p_oCounter.populate(loRS);
            MiscUtil.close(loRS);
            String lsStat = String.valueOf(p_nTranStat);
            String lsCondition ="";
        
            if (lsStat.length() > 1){
                for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                    lsCondition += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
                }

                lsCondition = " cTranStat IN (" + lsSQL.substring(2) + ")";
            } else{            
                lsCondition = " cTranStat = " + SQLUtil.toSQL(lsStat);
            }
            lsCondition = lsCondition + " AND sCtrCodex = " + SQLUtil.toSQL((String)getCounter("sCtrCodex"));
            lsSQL = getSQ_Master() + lsCondition + "  ORDER BY sTransNox DESC LIMIT 1";
           
            //open master
            loRS = p_oApp.executeQuery(lsSQL);
            p_oMaster = factory.createCachedRowSet();
            p_oMaster.populate(loRS);
            MiscUtil.close(loRS);

            p_oCounter.last();
            if (p_oCounter.getRow() <= 0) {
                p_sMessage = "No transaction was loaded.";
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Queue.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public boolean OpenOngoing(){
         if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
         
        p_sMessage = "";
        try {
            String lsSQL;
            ResultSet loRS;
            RowSetFactory factory = RowSetProvider.newFactory();

            lsSQL = getSQ_Ongoing();

            //open master
            loRS = p_oApp.executeQuery(lsSQL);
            p_oOngoing = factory.createCachedRowSet();
            p_oOngoing.populate(loRS);

            MiscUtil.close(loRS);

            p_oOngoing.last();
            if (p_oOngoing.getRow() <= 0) {
                p_sMessage = "No transaction was loaded.";
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Queue.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    public boolean OpenOnDispaly(){
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        p_sMessage = "";
        try {
            createDisplay();
            
            String lsSQL = getSQ_Counter() ;
            ResultSet loRS;
            //open master
            loRS = p_oApp.executeQuery(lsSQL);
             int lnRow = 1;
           while (loRS.next()){
              
               
                p_oDisplay.last();
                String lsCondition =" sCtrCodex = " + SQLUtil.toSQL(loRS.getString("sCtrCodex"));
                String foSQL = getSQ_Master();
               
                String lsStat = String.valueOf(p_nTranStat);
            
                if (lsStat.length() > 1){
                    for (int lnCtr = 0; lnCtr <= lsStat.length()-1; lnCtr++){
                        lsCondition += ", " + SQLUtil.toSQL(Character.toString(lsStat.charAt(lnCtr)));
                    }
                    lsCondition = lsCondition + " AND cTranStat IN (" + lsSQL.substring(2) + ")";
                } else{            
                    lsCondition = lsCondition + " AND cTranStat = " + SQLUtil.toSQL(lsStat);
                }
                
                lsCondition = lsCondition + " ORDER BY sTransNox DESC LIMIT 1";
                foSQL = foSQL + lsCondition;
                ResultSet foRS = p_oApp.executeQuery(foSQL);
                String transNo = "";
                String ctrcode = "";
                String ctrnumber = "";
                String transtat = "";
                while (foRS.next()){
                    transNo = foRS.getString("sTransNox");
                    ctrcode = foRS.getString("sCtrCodex");
                    ctrnumber = foRS.getString("sCtrNmber");
                    transtat = foRS.getString("cTranStat");
                    
                   
                }
                
                p_oDisplay.moveToInsertRow();
                initRowSet(p_oDisplay); 
                p_oDisplay.updateString("sCtrCodex", loRS.getString("sCtrCodex"));
                if(transNo.isEmpty()){
                    p_oDisplay.updateString("sTransNox", "");
                    p_oDisplay.updateString("sCtrNmber", "");
                    p_oDisplay.updateString("cTranStat", "");
                }else{
                    p_oDisplay.updateString("sTransNox", transNo);
                    p_oDisplay.updateString("sCtrCodex", ctrcode);
                    p_oDisplay.updateString("sCtrNmber", ctrnumber);
                    p_oDisplay.updateString("cTranStat", transtat);
                }
               
                p_oDisplay.insertRow();
                p_oDisplay.moveToCurrentRow();
                lnRow++;
                MiscUtil.close(foRS);


           }
           MiscUtil.close(loRS);
            
        } catch (SQLException ex) {
            Logger.getLogger(Queue.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    public String getSQ_Master(){
        String lsSQL = "";
                
        lsSQL = "SELECT" + 
                    "  IFNULL(sTransNox,'') sTransNox" +
                    ", IFNULL(dTransact,'') dTransact" +
                    ", IFNULL(sCtrNmber,'') sCtrNmber" +
                    ", IFNULL(sCtrCodex,'') sCtrCodex" +
                    ", IFNULL(dTimeStrt,'') dTimeStrt" +
                    ", IFNULL(dTimeStop,'') dTimeStop" +
                    ", IFNULL(cTranStat,'') cTranStat" +
                    ", IFNULL(sModified,'') sModified" +
                    ", IFNULL(dModified,'') dModified" +
                " FROM " + MASTER_TABLE+ 
                " WHERE ";
        
        return lsSQL;
    }
    public String getSQ_Counter(){
        String lsSQL = "";
                
        lsSQL = "SELECT" + 
                    "  IFNULL(sCtrCodex,'') sCtrCodex" +
                    ", IFNULL(sCtrDescx,'') sCtrDescx" +
                    ", IFNULL(sCompName,'') sCompName" +
                    ", IFNULL(cRecdStat,'') cRecdStat" +
                " FROM " + COUNTER_TABLE;
        
        return lsSQL;
    }
    public String getSQ_Ongoing(){
        String lsSQL = "";
                
        lsSQL = "SELECT" + 
                    "  IFNULL(sCtrCodex,'') sCtrCodex" +
                    ", IFNULL(sCtrNmber,'') sCtrNmber" +
                " FROM " + ONGOING_TABLE;
        
        return lsSQL;
    }
    private void createDisplay() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(4);

        meta.setColumnName(1, "sTransNox");
        meta.setColumnLabel(1, "sCtrCodex");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);
        
        meta.setColumnName(2, "sCtrCodex");
        meta.setColumnLabel(2, "sCtrCodex");
        meta.setColumnType(2, Types.VARCHAR);
        meta.setColumnDisplaySize(2, 2);
        
        meta.setColumnName(3, "sCtrNmber");
        meta.setColumnLabel(3, "sCtrNmber");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 4);
        
        meta.setColumnName(4, "cTranStat");
        meta.setColumnLabel(4, "cTranStat");
        meta.setColumnType(4, Types.CHAR);
        meta.setColumnDisplaySize(4, 1);
        
        p_oDisplay = new CachedRowSetImpl();
        p_oDisplay.setMetaData(meta);
    }
    private void createMaster() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(8);

        meta.setColumnName(1, "sTransNox");
        meta.setColumnLabel(1, "sTransNox");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 12);

        meta.setColumnName(2, "dTransact");
        meta.setColumnLabel(2, "dTransact");
        meta.setColumnType(2, Types.DATE);
        
        meta.setColumnName(3, "sCtrNmber");
        meta.setColumnLabel(3, "sCtrNmber");
        meta.setColumnType(3, Types.VARCHAR);
        meta.setColumnDisplaySize(3, 4);
        
        meta.setColumnName(4, "dTimeStrt");
        meta.setColumnLabel(4, "dTimeStrt");
        meta.setColumnType(4, Types.TIME);
        
        meta.setColumnName(5, "dTimeStop");
        meta.setColumnLabel(5, "dTimeStop");
        meta.setColumnType(4, Types.TIME);
        
        meta.setColumnName(6, "cTranStat");
        meta.setColumnLabel(6, "cTranStat");
        meta.setColumnType(6, Types.CHAR);
        meta.setColumnDisplaySize(6, 1);
        
        meta.setColumnName(7, "sModified");
        meta.setColumnLabel(7, "sModified");
        meta.setColumnType(7, Types.VARCHAR);
        meta.setColumnDisplaySize(7, 12);
        
        meta.setColumnName(8, "dModified");
        meta.setColumnLabel(8, "dModified");
        meta.setColumnType(8, Types.DATE);
        
        p_oMaster = new CachedRowSetImpl();
        p_oMaster.setMetaData(meta);
    }
    private void createCounter() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(6);

        meta.setColumnName(1, "sCtrCodex");
        meta.setColumnLabel(1, "sCtrCodex");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 2);

        meta.setColumnName(2, "sCtrDescx");
        meta.setColumnLabel(2, "sCtrDescx");
        meta.setColumnType(2, Types.VARCHAR);
        
        meta.setColumnName(3, "sCompName");
        meta.setColumnLabel(3, "sCompName");
        meta.setColumnType(3, Types.VARCHAR);
        
        meta.setColumnName(4, "cRecdStat");
        meta.setColumnLabel(4, "cRecdStat");
        meta.setColumnType(4, Types.CHAR);
        meta.setColumnDisplaySize(1, 1);
        
        
        meta.setColumnName(5, "sModified");
        meta.setColumnLabel(5, "sModified");
        meta.setColumnType(5, Types.VARCHAR);
        meta.setColumnDisplaySize(5, 12);
        
        meta.setColumnName(6, "dModified");
        meta.setColumnLabel(6, "dModified");
        meta.setColumnType(6, Types.DATE);
        
        p_oCounter = new CachedRowSetImpl();
        p_oCounter.setMetaData(meta);
       
    }private void createOngoing() throws SQLException{
        RowSetMetaData meta = new RowSetMetaDataImpl();

        meta.setColumnCount(2);

        meta.setColumnName(1, "sCtrCodex");
        meta.setColumnLabel(1, "sCtrCodex");
        meta.setColumnType(1, Types.VARCHAR);
        meta.setColumnDisplaySize(1, 2);

        meta.setColumnName(2, "sCtrNmber");
        meta.setColumnLabel(2, "sCtrNmber");
        meta.setColumnType(2, Types.VARCHAR);
        
        p_oOngoing = new CachedRowSetImpl();
        p_oOngoing.setMetaData(meta);
    }
    
    private int getColumnIndex(CachedRowSet loRS, String fsValue) throws SQLException{
        int lnIndex = 0;
        int lnRow = loRS.getMetaData().getColumnCount();
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            if (fsValue.equals(loRS.getMetaData().getColumnLabel(lnCtr))){
                lnIndex = lnCtr;
                break;
            }
        }
        
        return lnIndex;
    }
    
    private void initRowSet(CachedRowSet rowset) throws SQLException{
        java.sql.ResultSetMetaData cols = rowset.getMetaData();
        for(int n=1;n<=cols.getColumnCount();n++){
            switch(cols.getColumnType(n)){
                case java.sql.Types.BIGINT:
                case java.sql.Types.INTEGER:
                case java.sql.Types.SMALLINT:
                case java.sql.Types.TINYINT:
                    rowset.updateObject(n, 0);
                    break;
                case java.sql.Types.DECIMAL:
                case java.sql.Types.DOUBLE:
                case java.sql.Types.FLOAT:
                case java.sql.Types.NUMERIC:
                case java.sql.Types.REAL:
                    rowset.updateObject(n, 0.00);
                    break;
                case java.sql.Types.CHAR:
                case java.sql.Types.NCHAR:
                case java.sql.Types.NVARCHAR:
                case java.sql.Types.VARCHAR:
                    rowset.updateObject(n, "");
                    break;
                default:
                    rowset.updateObject(n, null);
            }
        }
    }
}
