/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qms.model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author user
 */
public class TableModel {
    private SimpleStringProperty index01;
    private SimpleStringProperty index02;
    
    
    public TableModel(String index01,
               String index02){
        
        this.index01 = new SimpleStringProperty(index01);
        this.index02 = new SimpleStringProperty(index02);
    }

    

    public String getIndex01(){return index01.get();}
    public void setIndex01(String index01){this.index01.set(index01);}
    
    public String getIndex02(){return index02.get();}
    public void setIndex02(String index02){this.index02.set(index02);}
    
}
