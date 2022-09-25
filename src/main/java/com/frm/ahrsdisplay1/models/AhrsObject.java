/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1.models;

import javafx.geometry.Point2D;

/**
 *
 * @author frive
 */
public class AhrsObject {
    
    Point2D  poly[];
    
    public AhrsObject(Point2D[] poly){
       this.poly=poly;
    }
    
    public Point2D[] rotate(double angle, double xc, double yc){
        Point2D  rot[]=new Point2D[poly.length];
        Point2D center=new Point2D(xc,yc);
        
        for(int i=0;i<poly.length;i++){
            rot[i]=transform(poly[i],center,angle);
        }
        
        return rot;
    }
    
    private Point2D transform(Point2D p, Point2D center,double angle){
    
        double xr=p.getX()-center.getX();
        double yr=(p.getY()-center.getY());
        double x=center.getX()+xr*Math.cos(angle*Math.PI/180)-yr*Math.sin(angle*Math.PI/180);
        double y=center.getY()+xr*Math.sin(angle*Math.PI/180)+yr*Math.cos(angle*Math.PI/180);
        
        return new Point2D(x,y);
        
        
    }
    
    public double[] getXarray(Point2D[] p){
        double x[]=new double[p.length];
        
        for(int i=0;i<p.length;i++){
            x[i]=p[i].getX();
        }
        
        return x;
    }
    
    public double[] getYarray(Point2D[] p){
        double x[]=new double[p.length];
        
        for(int i=0;i<p.length;i++){
            x[i]=p[i].getY();
        }
        
        return x;
    }
    
    public int getSize(){
        
        return poly.length;
    }
    
}
