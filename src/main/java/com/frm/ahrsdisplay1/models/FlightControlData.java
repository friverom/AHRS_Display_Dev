/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author frive
 */
public class FlightControlData {
    
    public StringProperty rx_data;
    public StringProperty flight_state;
    private int comErrors; //Checksum error counter
    public BooleanProperty connectionStatus;
    public DoubleProperty voltage;
    public DoubleProperty current;
    public DoubleProperty yaw;
    public DoubleProperty pitch;
    public DoubleProperty roll;
    public DoubleProperty altitude;
    public DoubleProperty temperature;
    
    private SerialComm serialcomm;
    
    //Class constructor
    public FlightControlData(SerialComm serialcomm){
        this.serialcomm=serialcomm;
        rx_data=new SimpleStringProperty();
        flight_state=new SimpleStringProperty();
        voltage=new SimpleDoubleProperty();
        current=new SimpleDoubleProperty();
        yaw=new SimpleDoubleProperty();
        pitch=new SimpleDoubleProperty();
        roll=new SimpleDoubleProperty();
        altitude=new SimpleDoubleProperty();
        temperature=new SimpleDoubleProperty();
        
        connectionStatus=new SimpleBooleanProperty(false);
        comErrors=0;
        
        //Data listener for RX event
        serialcomm.rx_data.addListener((v,oldValue,newValue)->{
            rx_data.set(newValue);
            connectionStatus.set(false);
            processData(newValue);
        });
        
        //Data listener for port disconnection
        serialcomm.portStatus.addListener((v,oldValue,newValue)->{
            connectionStatus.set(true);
        });
        
    };
    //Just for testing the concept
    public double getVoltage(){
        return voltage.doubleValue();
    }
    
    public double getCurrent(){
        return current.doubleValue();
    }
    
    //
    public Boolean getConnectionStatus(){
        return connectionStatus.getValue();
    }
    
    public int getComErrors(){
        return comErrors;
    }
    
    public void resetComErrors(){
        comErrors=0;
    }
    
    public void sendData(String data){
        String str=addCheckSum(data);
         serialcomm.tx_data(str);
    }
     //Process data from RX Buffer
    private int processData(String data){
        
        int status=0;
        
        String[] items=data.split(",");
        
        //Get checksum from data packet
        int chkSumTx=Integer.parseInt(items[items.length-1].substring(0, items[items.length-1].length()-2));
        
        //Calculate checksum from RX Buffer
        int chkSumLcl=getCheckSum(data);
        
        //If checksums are equal process data
        if(chkSumTx==chkSumLcl){
        switch (items[0]) {
            case "$dat1":
                voltage.set(Double.parseDouble(items[1]));
                break;
            
            case "$dat2":
                current.set(Double.parseDouble(items[1]));
                break;
                
            case "$dat3":
                flight_state.set(getDataString(data,5));
                yaw.set(Double.parseDouble(items[1]));
                pitch.set(Double.parseDouble(items[2]));
                roll.set(Double.parseDouble(items[3]));
                altitude.set(Double.parseDouble(items[4]));
                temperature.set(Double.parseDouble(items[5]));
                 break;
                
            default:
                status=1;
        }
        }
        //Indicate chksum error
        else{
            comErrors++;
            status=2;
        }
        
        return status;
    }
    
    //Calculate checksum from RX data
    private int getCheckSum(String data){
        
        //Remove checksum from RX data and generate a string without checksum
        String[] items=data.split(",");
        String str="";
        for(int i=0;i<items.length-1;i++){
            str+=items[i];
            if(i<items.length-2){
                str+=",";
            }
        }
      
        str+=",";
        
        //Calculate checksum
        int chksum=0;
        byte[] btys=str.getBytes();
        for(int i=0;i<btys.length;i++){
            chksum+=btys[i];
        }
        return chksum;
    }

    //Add checksum to end of string data
   private String addCheckSum(String data){
   
       int chkSum=0;
       byte[] btys=data.getBytes();
        for(int i=0;i<btys.length;i++){
            chkSum+=btys[i];
        }
        chkSum+=',';
        String str=String.format(",%d\n\r", chkSum);
        return data+str;
   }
   
   //Extract data from string
   private String getDataString(String data, int numData){
       
       String[] str=data.split(",");
       String txt="";
       for(int i=1;i<numData+1;i++){
           txt+=str[i];
           if(i<numData){
               txt+=",";
           }
       }
       return txt;
   
   }
   
    
}
