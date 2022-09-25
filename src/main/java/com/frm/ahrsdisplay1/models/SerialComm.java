/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1.models;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class handles the serial com ports.
 * Opens the selected port at the selected baud rate and it is packet delimited.
 * RX data will be available in rx_string variable instance of String Property
 * so it can be listened on the calling class
 * @author frive
 */
public class SerialComm {
    
    private static SerialPort[] portList;
    private static String[] baudRateList;
    private static SerialPort selectedPort; 
    private static String selectedBaudRate;
    public StringProperty rx_data; //RX buffer
    public BooleanProperty portStatus; //To notify if COM port is connected or not
    private int  timer; //This is a counter of seconds since last packet received
      
    //Class constructor. It creates an available com port list and also the 
    //available baud rates
    public SerialComm(){
        //Create COM port list
       portList=SerialPort.getCommPorts();
       
       //Create the Baud Rates ComboBox list
       
        baudRateList=new String[]{"9600","19200","38400","57600","115200"};
        
        //Create RX Buffer
        rx_data=new SimpleStringProperty();
        portStatus=new SimpleBooleanProperty(false);
        
         //Start eleapse timer for lost communication
         timer=0;
       Thread timer=new Thread(new ElapseTime());
       timer.setDaemon(true);
       timer.start();
    }
    
    //Open COM Port
    public boolean open_Port(SerialPort port, String selectedBaudRate){
        SerialComm.selectedPort=port;
        SerialComm.selectedBaudRate=selectedBaudRate;
        boolean portStatus=port.openPort();
        boolean baudRateStatus=port.setBaudRate(Integer.parseInt(selectedBaudRate));
        
        //Set data Listener
        delimitedDataListener();
        return portStatus || baudRateStatus;
         
    }
    
    //Check if Port is Open
    public boolean is_open(){
        return selectedPort.isOpen();
    }
    //Close Port
    public boolean close_Port(){
        this.removeDataListener();
        return selectedPort.closePort();
   }
    
   //Retrieve COM Ports
    public SerialPort[] getComPorts(){
        return this.portList;
    }
    
    //Retrieve Baud Rate List
    public String[]getBaudRate(){
        return baudRateList;
    }
    
   //Remove Data Listener
    public void removeDataListener(){
        selectedPort.removeDataListener();
    }
    //Transmit data
    public int tx_data(String data){
        return selectedPort.writeBytes(data.getBytes(), data.getBytes().length);
    }
    
    //Delimited Message Received Listener
    private void delimitedDataListener(){
        selectedPort.addDataListener(new SerialPortMessageListener(){
            
            //Will listen to RX data and COM port disconnection
            @Override
            public int getListeningEvents(){
                return (SerialPort.LISTENING_EVENT_DATA_RECEIVED)|(SerialPort.LISTENING_EVENT_PORT_DISCONNECTED);
            }
            
            //Data packet ends with \n\r characters
            @Override
            public byte[] getMessageDelimiter(){
                return new byte[]{(byte)0x0a, (byte)0x0d};
            }
            
            @Override
            public boolean delimiterIndicatesEndOfMessage(){
                return true;
            }
            
            @Override
            public void serialEvent(SerialPortEvent event) {
               
                timer=0; //Clear timer anytime a data packet is received
                
                //Check if event type is RX
                if ((event.getEventType() & SerialPort.LISTENING_EVENT_DATA_RECEIVED) == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                    portStatus.set(false);
                    byte[] delimitedMessage = event.getReceivedData();
                    rx_data.set(new String(delimitedMessage));
                }else{
                    portStatus.set(true);
                }

            }
        
    });
    }
    
    //This thread implements a timer to notify when RX is not active in the 
    //last 4 seconds.
    class ElapseTime implements Runnable{

        @Override
        public void run() {
            while(true){
                if(timer>=4){
                    portStatus.set(true);
                }
                timer++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
        }
    
    }
   
   
    
}


