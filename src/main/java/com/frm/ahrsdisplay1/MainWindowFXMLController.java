/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1;

import com.fazecast.jSerialComm.SerialPort;
import com.frm.ahrsdisplay1.models.FlightControlData;
import com.frm.ahrsdisplay1.models.Sim;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author frive
 */
public class MainWindowFXMLController implements Initializable {

    @FXML
    private ComboBox<SerialPort> cb_selectComPort;
  
    @FXML
    private ComboBox<String> cb_selectBaudRate;
    
    private SerialPort selectedPort;
    private String selectedBaudRate;
    private StringProperty rx_data;
    private FlightControlData fc;
   
    //Just for simulation
     private DoubleProperty voltage;
     private DoubleProperty amp;
     private DoubleProperty mAH;
     private DoubleProperty asp;
     private int voltage_state=0;
     
    FXMLLoader loader;
    Parent root;
    AhrsDisplay1FXMLController ahrs;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fc=new FlightControlData();
        SerialPort[] portsList=fc.getComPorts();
        //Get COM ports into Combo Box
        ArrayList<SerialPort> ports=new ArrayList<>();
        Collections.addAll(ports, portsList);
        cb_selectComPort.getItems().addAll(ports);
        
        //Set Baud Rate Combo Box
        String[] list=fc.getBaudRate();
        ArrayList<String> baudrateList=new ArrayList<>();
        Collections.addAll(baudrateList, list);
        cb_selectBaudRate.getItems().addAll(list);
        
        
        
        loader=new FXMLLoader();
        try {
            root=loader.load(getClass().getResource("AhrsDisplay1FXML.fxml").openStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ahrs=(AhrsDisplay1FXMLController)loader.getController();
        
        //Set data listeners
        
        fc.flight_state.addListener((v,o,n)->{
            ahrs.setflightState(n);
        });
       
        voltage=new SimpleDoubleProperty();
        amp=new SimpleDoubleProperty();
        mAH=new SimpleDoubleProperty();
        asp=new SimpleDoubleProperty();
        
        voltage.addListener((v,o,n)->{
            String str=String.format("%.1f", voltage.get());
            if(voltage.get()>15){
               ahrs.setText(0, 2, str, false, Color.BLACK);
            }else if(voltage.get()>5){
               ahrs.setText(0, 2, str, false, Color.RED);
            }else{
                ahrs.setText(0, 2, str, true, Color.RED);
            }
            
        });
        
        amp.addListener((v,o,n)->{
            String str=String.format("%.1f", amp.get());
            ahrs.setText(0, 3, str);
        });
        
        mAH.addListener((v,o,n)->{
            String str=String.format("%.1f", mAH.get());
            ahrs.setText(0, 4, str);
        });
        
        asp.addListener((v,o,n)->{
            String str=String.format("%.1f", asp.get());
            ahrs.setAirSpeed(asp.get());
        });
    }    

    @FXML
    private void selectComPortAction(ActionEvent event) {
        selectedPort=cb_selectComPort.getSelectionModel().getSelectedItem();
        
    }

    @FXML
    private void setBaudRateAction(ActionEvent event) {
        selectedBaudRate=cb_selectBaudRate.getSelectionModel().getSelectedItem();
        
    }

    @FXML
    private void openPortAction(ActionEvent event) throws IOException {
        boolean stat=fc.open_Port(selectedPort, selectedBaudRate);
        Stage stage=new Stage();
           
        Scene scene=new Scene(root);
     //   scene.getStylesheets().add(getClass().getResource("/Style/LineChartStyle.css").toExternalForm());
        stage.setScene(scene);
     //   stage.setTitle("AHRS");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
        
        Sim voltage_t=new Sim(voltage,15,15,90,0.02);
        Sim amp_t=new Sim(amp,50,50,90,0.01);
        Sim mAH_t=new Sim(mAH,3000,3000,45,0.01);
        Sim asp_t=new Sim(asp,90,100,0,0.01);
        voltage_t.startSim();
        amp_t.startSim();
        mAH_t.startSim();
        asp_t.startSim();
        
        
        
    }

    @FXML
    private void selectBaudRateAction(ActionEvent event) {
        fc.close_Port();
    }
    
    public void exitApp(){
        fc.close_Port();
        ahrs.exitApp();
        Platform.exit();
    }
    
}
