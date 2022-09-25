/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1.models;

import java.math.BigDecimal;
import javafx.concurrent.Task;

/**
 *
 * @author frive
 */
public class ElapseTimer extends Task<Long>{
    
    private long t=0;
   
    
    @Override
    protected Long call() throws Exception {

        
        while(!isCancelled()){
            updateValue(t);
            t++;
            Thread.sleep(1000);
        }

        return t;
    }
    
   
    
}
