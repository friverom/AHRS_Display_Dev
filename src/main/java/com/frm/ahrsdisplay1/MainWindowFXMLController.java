/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1;

import com.fazecast.jSerialComm.SerialPort;
import com.frm.ahrsdisplay1.models.FlightControlData;
import com.frm.ahrsdisplay1.models.SerialComm;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

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
    
    private SerialComm comport;
    private SerialPort selectedPort;
    private String selectedBaudRate;
    private StringProperty rx_data;
    
    private FlightControlData fc;
    
    FXMLLoader loader;
    Parent root;
    AhrsDisplay1FXMLController ahrs;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comport=new SerialComm();
        SerialPort[] portsList=comport.getComPorts();
        //Get COM ports into Combo Box
        ArrayList<SerialPort> ports=new ArrayList<>();
        Collections.addAll(ports, portsList);
        cb_selectComPort.getItems().addAll(ports);
        
        //Set Baud Rate Combo Box
        String[] list=comport.getBaudRate();
        ArrayList<String> baudrateList=new ArrayList<>();
        Collections.addAll(baudrateList, list);
        cb_selectBaudRate.getItems().addAll(list);
        
        fc=new FlightControlData(comport);
        
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
        /*
        //Yaw
        fc.yaw.addListener((v,o,n)->{
            ahrs.setHeading(n.doubleValue());
        });
        
        //Roll
        fc.roll.addListener((v,o,n)->{
            ahrs.setRoll(n.doubleValue());
        });
        
        //Pitch
        fc.pitch.addListener((v,o,n)->{
            ahrs.setPitch(n.doubleValue());
        });
        
        //Altitude
        fc.altitude.addListener((v,o,n)->{
            ahrs.setAltitude(n.doubleValue());
        });*/
        
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
        boolean stat=comport.open_Port(selectedPort, selectedBaudRate);
        Stage stage=new Stage();
           
        Scene scene=new Scene(root);
     //   scene.getStylesheets().add(getClass().getResource("/Style/LineChartStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("AHRS");
        stage.show();
        
        
        
        
    }

    @FXML
    private void selectBaudRateAction(ActionEvent event) {
        comport.close_Port();
    }
    
    public void exitApp(){
        comport.close_Port();
        Platform.exit();
    }
    
}
