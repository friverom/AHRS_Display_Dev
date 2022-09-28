/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1.models;

import javafx.beans.property.DoubleProperty;

/**
 *
 * @author frive
 */
public class Sim implements Runnable{
    
    DoubleProperty value;
    double amplitude;
    double bias;
    double phase;
    double time;
    Thread t;
    boolean running=false;
    
    public Sim(DoubleProperty value, double amplitude, double bias, double phase, double time){
        this.value=value;
        this.amplitude=amplitude;
        this.bias=bias;
        this.phase=phase*Math.PI/180.0;
        this.time=time;
    }
    
    public void startSim(){
        t=new Thread(this);
        t.setDaemon(true);
        t.start();
    }
    
    public void stopSim(){
        this.running=false;
    }
    
    @Override
    public void run() {
        this.running=true;
        int i=0;
        
        while(running){
            value.set(Math.sin(i*time+phase)*amplitude+bias);
            i++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
    

