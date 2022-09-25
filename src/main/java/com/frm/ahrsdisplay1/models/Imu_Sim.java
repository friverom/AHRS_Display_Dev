/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1.models;

import javafx.concurrent.Task;

/**
 *
 * @author frive
 */
public class Imu_Sim extends Task<Double>{

    private final double amplitude;
    private final double bias;
    private final double angle;
    private final double delay;
    
    public Imu_Sim(double amplitude, double bias, double angle, double t){
        this.bias=bias;
        this.amplitude=amplitude;
        this.angle=angle;
        this.delay=t;
    }
    
        
    @Override
    protected Double call() throws Exception {
        Double value=0.0;
        int i=0;
        
        while(!isCancelled()){
            value=bias+amplitude*Math.sin(i*delay*Math.PI/180+angle*Math.PI/180);
            i++;
            updateValue(value);
            Thread.sleep(100);
        }
        
        return value;    
    }

   

}
