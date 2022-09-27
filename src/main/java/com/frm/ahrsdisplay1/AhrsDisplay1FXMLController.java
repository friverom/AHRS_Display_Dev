/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frm.ahrsdisplay1;

import com.frm.ahrsdisplay1.models.AhrsObject;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
/**
 * FXML Controller class
 *
 * @author frive
 */
public class AhrsDisplay1FXMLController implements Initializable {

    @FXML
    private Canvas ahLayer; //Will Hold the AH and rotating text and graphics
    @FXML
    private Canvas txtLayer; //Holds all text information
    @FXML
    private Canvas staticLayer; //Holds all graphics that does not change with roll, pitch and yaw
    @FXML
    private Canvas fltParamLayer; //Holds Air speed and altitude indicators
    @FXML
    private Canvas navLayer; //Holds all navigation pointers

    //Constants
    private final double AHRS_MAX_WIDTH=500; //Canvas size
    private final double AHRS_MAX_HEIGHT=500;
    
    //Roll pointer base and height
    private final double AHRS_RP_YH=80; //Roll pointer y position
    private final double AHRS_RP_XW=8; //Roll pointer base lenght
    
    //Heading label
    private final double AHRS_HDG_BARS=9; //# of 10 Degrees Bars on Heading
    private final double AHRS_HDG_RES=AHRS_MAX_WIDTH/((AHRS_HDG_BARS-1)*10);
    private final double AHRS_HDG_HEIGHT=25;
    private final double AHRS_HDG_POINTER_POSY=25;
    HeadingDisplay hdgLabels;
    
    //Air speed line start point
    private final double AHRS_ASP_POS_X=20; 
    private final double AHRS_ASP_POS_Y=60;
    private final double AHRS_ASP_LENGHT=AHRS_MAX_HEIGHT-AHRS_ASP_POS_Y*2;
    
    //Altitude constants
    private final double AHRS_ALT_POS_X=AHRS_MAX_WIDTH-20;
    private final double AHRS_ALT_POS_Y=60;
    private final double AHRS_ALT_LENGTH=AHRS_MAX_HEIGHT-AHRS_ALT_POS_Y*2;
    
    //Pitch angle lines and labels
    final int pitchAngles[]={-30,-20,-10,10,20,30};
    final String pitchAngleLabels[]={"-30","-20","-10","10","20","30"};
        
    private GraphicsContext gcAhLayer,gcTxtLayer,gcStaticLayer;
    private GraphicsContext gcfltPLayer,gcNavLayer;
    
    //Variables to store AHRS state
    private double roll; //Roll actual value
    private double pitch; //Pitch actual value
    private double heading; //Heading actual value
    
    //Variables to altitude settings
    private double altitude;
    private double minAlt;
    private double maxAlt;
   
    private long flightTime;
    private String elapseTimer="00:00:00";

    //Variables for Air speed indicator
    private double minSpeed;
    private double maxSpeed;
    private double airSpeed; //Air speed
    private double groundSpeed; //Ground speed. GPS
     private double verticalSpeed;
    
    //Define the Pointer for the roll data
    private AhrsObject rollPointerDown, rollPointerUp;
    
    
    //Define the air speed pointer
     private AhrsObject airSpeedPointer;
   
      
    //Define Text array list
    ArrayList<TextNotes>[] msg_list=new ArrayList[6]; //6 layers of text
    private int active_text_msg=0; //Text layer to display
   
   
    FlightTimer ft;
    StringProperty elapse_t;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //Sets all canvas to same size
        ahLayer.setWidth(AHRS_MAX_WIDTH);
        ahLayer.setHeight(AHRS_MAX_HEIGHT);
        
        txtLayer.setWidth(AHRS_MAX_WIDTH);
        txtLayer.setHeight(AHRS_MAX_HEIGHT);
        
        staticLayer.setWidth(AHRS_MAX_WIDTH);
        staticLayer.setHeight(AHRS_MAX_HEIGHT);
        
        fltParamLayer.setWidth(AHRS_MAX_WIDTH);
        fltParamLayer.setHeight(AHRS_MAX_HEIGHT);
        
        navLayer.setWidth(AHRS_MAX_WIDTH);
        navLayer.setHeight(AHRS_MAX_HEIGHT);
        
        gcAhLayer=ahLayer.getGraphicsContext2D();
        gcTxtLayer=txtLayer.getGraphicsContext2D();
        gcStaticLayer=staticLayer.getGraphicsContext2D();
        gcfltPLayer=fltParamLayer.getGraphicsContext2D();
        gcNavLayer=navLayer.getGraphicsContext2D();
        
        gcAhLayer.setFill(Color.TRANSPARENT);
        gcAhLayer.fillRect(0, 0, gcAhLayer.getCanvas().getWidth(), gcAhLayer.getCanvas().getHeight());
        
        gcTxtLayer.setFill(Color.TRANSPARENT);
        gcTxtLayer.fillRect(0, 0, gcTxtLayer.getCanvas().getWidth(), gcTxtLayer.getCanvas().getHeight());
        
        gcfltPLayer.setFill(Color.TRANSPARENT);
        gcfltPLayer.fillRect(0, 0, gcStaticLayer.getCanvas().getWidth(), gcStaticLayer.getCanvas().getHeight());
        
        gcNavLayer.setFill(Color.TRANSPARENT);
        gcNavLayer.fillRect(0, 0, gcStaticLayer.getCanvas().getWidth(), gcStaticLayer.getCanvas().getHeight());
     
       //Create all text layer
       for(int i=0; i<msg_list.length; i++){
           msg_list[i]=new ArrayList<>();
       }
       
       //Initialize Text Layer 0
       String label_0="KTS,FTS,VDC ,AMP ,mAH ,QNH ,OAT ,FT ";
       Point2D[] posXY={new Point2D(20,50),
       new Point2D(AHRS_MAX_WIDTH-30,50),
       new Point2D(80,55),
       new Point2D(80,70),
       new Point2D(80,85),
       new Point2D(AHRS_MAX_WIDTH-130,AHRS_MAX_HEIGHT-55),
       new Point2D(AHRS_MAX_WIDTH-130,70),
       new Point2D(AHRS_MAX_WIDTH-130,55)
       };
       
       String txt=" , ,24.5,50.3,17.537,1013,27.0,00:00:00";
            
        this.setLabel(0, label_0,posXY);
        this.setTextLayer(0, txt);
          
        elapse_t=new SimpleStringProperty();
        ft=new FlightTimer(elapse_t);
        elapse_t.addListener((v,o,n)->{
            setText(0,7,elapse_t.get());
        });
        
        ft.startTimer();
        
        hdgLabels = new HeadingDisplay();
            
        roll=0;
        pitch=0;
        heading=0;
        airSpeed=0;
        flightTime=0;
        this.setMaxAirSpeed(200.0);
        this.setMaxAltitude(2000);
        this.setMinAltitude(1000);
        
        drawStaticObjects(gcStaticLayer,60,20);
        //Create the triangle pointer for roll
        rollPointerDown=new AhrsObject(new Point2D[]{new Point2D( AHRS_MAX_WIDTH/2,AHRS_RP_YH), 
            new Point2D( AHRS_MAX_WIDTH/2-AHRS_RP_XW,AHRS_RP_YH+AHRS_RP_XW),
            new Point2D( AHRS_MAX_WIDTH/2+AHRS_RP_XW,AHRS_RP_YH+AHRS_RP_XW)});
      
        //Create air speed pointer
            airSpeedPointer=new AhrsObject(new Point2D[]{new Point2D( AHRS_ASP_POS_X+3,AHRS_ASP_POS_Y+AHRS_ASP_LENGHT), 
            new Point2D(AHRS_ASP_POS_X+4+AHRS_RP_XW,AHRS_ASP_POS_Y+AHRS_ASP_LENGHT-AHRS_RP_XW/2),
            new Point2D(AHRS_ASP_POS_X+4+AHRS_RP_XW,AHRS_ASP_POS_Y+AHRS_ASP_LENGHT+AHRS_RP_XW/2 )});
        
            updateAHRS();
    }   
    
    //Actualize flight information
    //state string "Yaw, pitch, roll, altitude, temperature"
    /**
     * Actualize flying state parameters
     * @param state CSV "Yaw,pitch,roll,altitude,temperature"
     */
    public void setflightState(String state){
    
        String[] str=state.split(",");
        this.heading=Double.parseDouble(str[0]);
        if(this.heading<0){
            this.heading=this.heading+360;
        }
        this.pitch=Double.parseDouble(str[1]);
        this.roll=Double.parseDouble(str[2]);
        this.altitude=Double.parseDouble(str[3]);
        
        setText(0,6,str[4],true,Color.RED); //Display OAT
        updateAHRS();
        
        
    }
    
    /**
     * Set text for active Layer
     *
     * @param index Active layer
     * @param state CSV "msg1, msg2, msg3, msg4, msg5, msg6, msg7, msg8
     */
    public void setTextLayer(int index, String state) {

        String[] str = state.split(",");
        
        //Check if num of msgs are greater than 8
        int l = str.length;
        if(l>8){
            l=8;
        }
        for (int i = 0; i < l; i++) {
            msg_list[index].get(i).text = str[i];
        }
        updateAHRS();
    }

    /**
     * Set Label for each message on indicated layer
     * @param list_index layer to set labels
     * @param label CSV "lbl1,lbl2,lbl3,lbl4,lbl5,lbl6,lbl7,lbl8
     * @param pos Position on AHRS for each label. It is an Array of Point2D
     */   
    public void setLabel(int list_index, String label, Point2D[] pos) {
        String[] items = label.split(",");
        int l = items.length;
        int p = pos.length;

        if ((l == p) && l < 9) {
            create_msg_list(msg_list[list_index], items.length);

            for (int i = 0; i < items.length; i++) {
                msg_list[list_index].get(i).label = items[i];
            }

            //Should throw exception if items.lenght != posXY.lenght
            for (int i = 0; i < pos.length; i++) {
                msg_list[list_index].get(i).posX = pos[i].getX();
                msg_list[list_index].get(i).posY = pos[i].getY();
            }
        }
    }
    /**
     * Set Text for individual message on indicated layer
     * @param index layer
     * @param msg Message to set
     * @param text Message
     */
    public void setText(int index, int msg, String text) {

        if (msg < 8) {
            msg_list[active_text_msg].get(msg).setText(text);
        }
    }
    /**
     * Set Text for individual message on indicated layer plus color and blink state
     * @param index active message layer
     * @param msg message to write to
     * @param text message text
     * @param blink true for blink
     * @param color Color
     */
    public void setText(int index, int msg, String text, boolean blink, Paint color){
    
        if (msg < 8) {
            msg_list[active_text_msg].get(msg).setText(text);
            msg_list[active_text_msg].get(msg).setColor(color);
            if(blink){
                msg_list[active_text_msg].get(msg).startBlink();
            }else{
                msg_list[active_text_msg].get(msg).stopBlink();
            }
            
        }
        
    }
    /**
     * Set the message text Color
     * @param msg Message to set color
     * @param color Color value of class Paint
     */
    public void setTextColor(int msg, Paint color) {
        if (msg < 8) {
            msg_list[active_text_msg].get(msg).setColor(color);
        }
    }
    /**
     * Set message text to blink
     * @param msg message index
     */
    public void startBlink(int msg) {
        if (msg < 8) {
            msg_list[active_text_msg].get(msg).startBlink();
        }
    }
    /**
     * Select the active text layer to display om AHRS
     * @param index Active layer number
     */
    public void selectActiveTextLayer(int index) {
        if (index < 6) {
            this.active_text_msg = index;
        }
    }
 
    /**
     * Set minimum airspeed for indicator
     * @param minAirspeed 
     */
    public void setMinAirSpeed(double minAirspeed){
        this.minSpeed=minAirspeed;
    }
    /**
     * Set maximum airspeed for indicator
     * @param maxAirspeed 
     */
    public void setMaxAirSpeed(double maxAirspeed){
        double maxS=Math.ceil(maxAirspeed);
        this.maxSpeed=((int)maxS/10)*10;
        
    }

    /**
     * Set Maximum altitude for indicator
     * @param altitude 
     */
    public void setMaxAltitude(double altitude){
    
        double maxA=Math.ceil(altitude);
        this.maxAlt=((int)maxA/10)*10;
    }
    /**
     * Set minimum altitude for indicator
     * @param altitude 
     */
    public void setMinAltitude(double altitude){
        
        double minA=Math.floor(altitude);
        this.minAlt=((int)minA/10)*10;
        
    }
    
    //Updates the AHRS Display
    private void updateAHRS(){
    
        updateAH(gcAhLayer);
        updateTxtLayer(gcTxtLayer);
        updateFltData(gcfltPLayer,this.heading,this.airSpeed,this.altitude);
        
    }
    /**
     * Creates the AH in the GC and draw the horizon line according to Roll and Pitch
     * information
     * @param gc Graphics Context to draw the AH
    
     */
    private void updateAH(GraphicsContext gc) {

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                //Generate the ground color
                gc.clearRect(0, 0,  gc.getCanvas().getWidth(),  gc.getCanvas().getHeight());
                gc.setFill(Color.BROWN);
                gc.fillRect(0, 0, gc.getCanvas().getWidth(),  gc.getCanvas().getHeight());

                //Compute roll and pitch
                double x = gc.getCanvas().getWidth() / 2 * Math.sin(pitch * Math.PI / 180);
                double y = (gc.getCanvas().getHeight() / 2) * Math.tan(roll * Math.PI / 180);
                gc.setFill(Color.AQUA);
                gc.fillPolygon(new double[]{0, gc.getCanvas().getWidth() , gc.getCanvas().getWidth(), 0},
                        new double[]{0, 0, gc.getCanvas().getWidth() / 2 + y + x, gc.getCanvas().getWidth() / 2 - y + x}, 4);
                
                //Draw triangle pointer around center of AHRS
                gc.setFill(Color.BLACK);
                Point2D[] t=rollPointerDown.rotate(roll, gc.getCanvas().getWidth()/2, gc.getCanvas().getHeight()/2);
                gc.fillPolygon(rollPointerDown.getXarray(t), rollPointerDown.getYarray(t),rollPointerDown.getSize());
                
                //Draw pitch angle lines and rotate them around center of AHRS
                gc.setFill(Color.BLACK);
                gc.setLineWidth(1);
                double x1;
                gc.save(); //Save actual GC
                for (int z = 0; z < pitchAngles.length; z++) {
                    //Rotate GC around center of AHRS
                    Rotate r=new Rotate(roll,gc.getCanvas().getWidth()/2,gc.getCanvas().getHeight()/2);
                    gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(),r.getTx(), r.getTy());
                    
                    //Calculate vertical position of pitch line
                    x1 = gc.getCanvas().getWidth() / 2 * Math.sin(pitchAngles[z] * Math.PI / 180);
                    
                    //Draw lines and labels in the rotated GC
                    gc.strokeLine(gc.getCanvas().getWidth() / 2 - 40, gc.getCanvas().getHeight() / 2 + x1, gc.getCanvas().getWidth() / 2 + 40, gc.getCanvas().getHeight() / 2 + x1);
                    gc.fillText(pitchAngleLabels[z], gc.getCanvas().getWidth() / 2 + 43, gc.getCanvas().getHeight() / 2 + x1 + 3);
                    gc.fillText(pitchAngleLabels[z], gc.getCanvas().getWidth() / 2 - 60, gc.getCanvas().getHeight() / 2 + x1 + 3);
                }
                gc.restore();
                
                
            }
        });

    }
    
    /**
     * Updates all text values in the AHRS
     * @param gc Graphics context to draw text
     * 
     */
    public void updateTxtLayer(GraphicsContext gc) {

        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        
                        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                        for(TextNotes m:msg_list[active_text_msg]){
                            gc.setFill(m.color);
                            gc.setFont(Font.font(m.fontSize));
                            gc.fillText(m.label+m.text, m.posX, m.posY);
                            
                            
                        }
                        
                        
                        
                        
                    }
                });

            }
        });
    }
    
    /**
     * This method draws a rectangle with black background, white letters a ed border
     * @param gc
     * @param text 
     * @param x
     * @param y
     * @param width
     * @param height 
     */
    private void drawTextWindow(GraphicsContext gc, String text, double x, double y, double width, double height) {
        gc.setFill(Color.BLACK);
        gc.fillRect(x,y,width,height);
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeRect(x,y,width,height);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(15));
        gc.fillText(text, x+7, y+height-6);

    }
    
    
    private void drawStaticObjects(GraphicsContext gc, double max_roll, double max_pitch){
        
        final String angleLabels[]={"60","45","30","20","10","00","10","20","30","45","60"};
        final int angleValues[]={-60,-45,-30,-20,-10,0,10,20,30,45,60};
        final int tickMarkssmal[]={-55,-50,-40,-35,-25,-15,-10,-5,5,15,25,35,40,50,55};
                
        //Draw Roll Angles
        //Compute arc position
        double rad=gc.getCanvas().getHeight()/2-AHRS_RP_YH; //Radius of arc
        
        //Position of arc
        double xp=gc.getCanvas().getWidth()/2-rad;
        double yp=gc.getCanvas().getHeight()/2-rad;
        //Arc width and height
        double aw=2*rad;
        double ah=2*rad;
        
        //draw arc
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setFill(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeArc(xp, yp,aw, ah, 30, 120, ArcType.OPEN);
        
        //Draw tick marks
       
        gc.setLineWidth(1);
        for(int i=0;i<angleValues.length;i++){
            gc.save();
            Rotate r=new Rotate(angleValues[i],gc.getCanvas().getWidth()/2,gc.getCanvas().getHeight()/2);
            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(),r.getTx(), r.getTy());
            gc.strokeLine(gc.getCanvas().getWidth()/2, AHRS_RP_YH, gc.getCanvas().getWidth()/2,  AHRS_RP_YH-10);
            gc.fillText(angleLabels[i], gc.getCanvas().getWidth()/2-7, AHRS_RP_YH-15);
            gc.restore();
        }
        
        //Draw 5 degrees tick marks
        for(int j=0;j<tickMarkssmal.length;j++){
         gc.save();
            Rotate r=new Rotate(tickMarkssmal[j],gc.getCanvas().getWidth()/2,gc.getCanvas().getHeight()/2);
            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(),r.getTx(), r.getTy());
            gc.strokeLine(gc.getCanvas().getWidth()/2, AHRS_RP_YH, gc.getCanvas().getWidth()/2,  AHRS_RP_YH-5);
            gc.restore();
        }
       
        
        //Draw Fixed Roll Pointer on top of arc
        rollPointerUp=new AhrsObject(new Point2D[]{new Point2D( gc.getCanvas().getWidth()/2,AHRS_RP_YH), new Point2D( gc.getCanvas().getWidth()/2-AHRS_RP_XW,AHRS_RP_YH-AHRS_RP_XW),new Point2D( gc.getCanvas().getWidth()/2+AHRS_RP_XW,AHRS_RP_YH-AHRS_RP_XW)});
        gc.setFill(Color.BLACK);
        Point2D[] tu=rollPointerUp.rotate(0, gc.getCanvas().getWidth()/2, gc.getCanvas().getHeight()/2);
        gc.fillPolygon(rollPointerUp.getXarray(tu), rollPointerUp.getYarray(tu),rollPointerUp.getSize());
        
        //Draw AH Indicator at center
        gc.setFill(Color.BLACK);
        gc.setLineWidth(3);
                gc.strokePolyline(new double[]{gc.getCanvas().getWidth()/2-80, gc.getCanvas().getWidth()/2-5, gc.getCanvas().getWidth()/2, gc.getCanvas().getWidth()/2+5,gc.getCanvas().getWidth()/2+80},
                       new double[]{gc.getCanvas().getHeight()/2, gc.getCanvas().getHeight()/2, gc.getCanvas().getHeight()/2+5, gc.getCanvas().getHeight()/2,gc.getCanvas().getHeight()/2}, 5);
     
        //Draw heading pointer
        AhrsObject headingPointer=new AhrsObject(new Point2D[]{new Point2D( gc.getCanvas().getWidth()/2,AHRS_HDG_POINTER_POSY), new Point2D( gc.getCanvas().getWidth()/2-AHRS_RP_XW,AHRS_HDG_POINTER_POSY-AHRS_RP_XW),new Point2D( gc.getCanvas().getWidth()/2+AHRS_RP_XW,AHRS_HDG_POINTER_POSY-AHRS_RP_XW)});
        gc.setFill(Color.BLACK);
        Point2D[] hp=headingPointer.rotate(0, gc.getCanvas().getWidth()/2, gc.getCanvas().getHeight()/2);
        gc.fillPolygon(headingPointer.getXarray(hp), headingPointer.getYarray(hp),headingPointer.getSize());
        
        //Draw Air speed Indicator
        //Draw vertical line for speed indication
        gc.setLineWidth(10);
        gc.setStroke(Color.LIMEGREEN);
        gc.strokeLine(AHRS_ASP_POS_X, AHRS_ASP_POS_Y, AHRS_ASP_POS_X, AHRS_ASP_POS_Y+AHRS_ASP_LENGHT);
        
        //Draw airspeed tick marks
        
        double delta=10;
        int dec=2;
        int num_dec=(int)this.maxSpeed/10;
        while(num_dec>10){
            num_dec=(int)(this.maxSpeed/(delta*dec));
            delta*=dec;
            dec++;
        }
        gc.setLineWidth(1);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));
        
        for(int i=0;i<num_dec+1;i++){
          gc.strokeLine(AHRS_ASP_POS_X, i*delta*AHRS_ASP_LENGHT/maxSpeed+AHRS_ASP_POS_Y, AHRS_ASP_POS_X+15,i*delta*AHRS_ASP_LENGHT/maxSpeed+AHRS_ASP_POS_Y);
          int t_speed=(int)(maxSpeed-i*delta);
          String txt=String.format("%d", t_speed);
          gc.fillText(txt, AHRS_ASP_POS_X+15,5+ i*delta*AHRS_ASP_LENGHT/maxSpeed+AHRS_ASP_POS_Y);
        }
        
        //Draw speed small tick marks
        for(int i=0;i<num_dec;i++){
          gc.strokeLine(AHRS_ASP_POS_X, i*delta*AHRS_ASP_LENGHT/maxSpeed+AHRS_ASP_POS_Y+AHRS_ASP_LENGHT/maxSpeed*delta/2, AHRS_ASP_POS_X+10,i*delta*AHRS_ASP_LENGHT/maxSpeed+AHRS_ASP_POS_Y+AHRS_ASP_LENGHT/maxSpeed*delta/2);
        }
        
        //Draw Altitude bar indicator
        gc.setLineWidth(10);
        gc.setStroke(Color.LIMEGREEN);
        gc.strokeLine(AHRS_ALT_POS_X, AHRS_ALT_POS_Y, AHRS_ALT_POS_X, AHRS_ALT_POS_Y+AHRS_ALT_LENGTH);
        
        //Draw altitude tick marks
        gc.setLineWidth(1);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));
        
        double deltaAlt=100;
        int decAlt=2;
        int num_dec_alt=(int)((this.maxAlt-this.minAlt)/deltaAlt);
        while(num_dec_alt>100){
            num_dec_alt=(int)((this.maxAlt-this.minAlt)/(deltaAlt*decAlt));
            deltaAlt*=decAlt;
            decAlt++;
        }
        for(int i=0;i<num_dec_alt+1;i++){
          gc.strokeLine(AHRS_ALT_POS_X, i*deltaAlt*AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt)+AHRS_ALT_POS_Y, AHRS_ALT_POS_X-15,i*deltaAlt*AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt)+AHRS_ALT_POS_Y);
          int t_alt=(int)(this.maxAlt-i*deltaAlt);
          String txtAlt=String.format("%d", t_alt);
          gc.fillText(txtAlt, AHRS_ALT_POS_X-45,5+ i*deltaAlt*AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt)+AHRS_ALT_POS_Y);
        }
        
        //Draw altitude small tick marks
        for(int i=0;i<num_dec_alt;i++){
          gc.strokeLine(AHRS_ALT_POS_X, i*deltaAlt*AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt)+AHRS_ALT_POS_Y+AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt)*deltaAlt/2, AHRS_ALT_POS_X-10,i*deltaAlt*AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt)+AHRS_ALT_POS_Y+AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt)*deltaAlt/2);
        }
        
    }
    
    /**
     * Updates the heading ribbon on top of display
     * @param gc
     * @param heading 
     */
    private void updateHeading(GraphicsContext gc, double heading) {

        
        String[] labels = this.hdgLabels.getHeadingLabels(heading,(int)AHRS_HDG_BARS);

        int hdg = (int) heading / 10; //Get Integer part of heading
        double dHdg = heading - hdg * 10;
        double xPos = gc.getCanvas().getWidth() / (AHRS_HDG_BARS - 1) - dHdg * AHRS_HDG_RES;
        gc.setLineWidth(1);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(15));
        for (int i = 0; i < AHRS_HDG_BARS - 1; i++) {
            gc.strokeLine(xPos + i * gc.getCanvas().getWidth() / (AHRS_HDG_BARS - 1), AHRS_HDG_HEIGHT, xPos + i * gc.getCanvas().getWidth() / (AHRS_HDG_BARS - 1), AHRS_HDG_HEIGHT - 10);
            gc.fillText(labels[i],xPos + i * gc.getCanvas().getWidth() / (AHRS_HDG_BARS - 1)-7,AHRS_HDG_HEIGHT-12);
        }

        for (int i = 0; i < (AHRS_HDG_BARS - 1) * 2; i++) {
            gc.strokeLine(xPos + i * gc.getCanvas().getWidth() / (AHRS_HDG_BARS - 1) / 2, AHRS_HDG_HEIGHT, xPos + i * gc.getCanvas().getWidth() / (AHRS_HDG_BARS - 1) / 2, AHRS_HDG_HEIGHT - 5);

        }
        //Display heading angle
        String headingTxt = String.format("%03.0f", heading);
        drawTextWindow(gc, headingTxt, gc.getCanvas().getWidth() / 2 - 19, 1, 37, 20);

    }
    
    private void updateSpeed(GraphicsContext gc, double speed){
    
        //Convert airspeed to pixel position
        double posY=speed*AHRS_ASP_LENGHT/this.maxSpeed;
      
        
        //Draw airspeed pointer
             
        AhrsObject pointer=new AhrsObject(new Point2D[]{new Point2D( AHRS_ASP_POS_X-3,AHRS_ASP_POS_Y+AHRS_ASP_LENGHT-posY), 
            new Point2D(AHRS_ASP_POS_X-4-AHRS_RP_XW,AHRS_ASP_POS_Y+AHRS_ASP_LENGHT-AHRS_RP_XW/2-posY),
            new Point2D(AHRS_ASP_POS_X-4-AHRS_RP_XW,AHRS_ASP_POS_Y+AHRS_ASP_LENGHT+AHRS_RP_XW/2-posY)});
        
        gc.setFill(Color.BLACK);
        Point2D[] hp=pointer.rotate(0, gc.getCanvas().getWidth()/2, gc.getCanvas().getHeight()/2);
        gc.fillPolygon(pointer.getXarray(hp), pointer.getYarray(hp),pointer.getSize());
        
        
        //display airspeed text
        String as = String.format("%.0f", speed);
        drawTextWindow(gc, as, AHRS_ASP_POS_X + 12,AHRS_ASP_POS_Y+AHRS_ASP_LENGHT-posY-10 , 37, 20);
     //   drawTextWindow(gc, as, AHRS_ASP_POS_X + 10, gc.getCanvas().getHeight() / 2-10, 37, 20);
        
    }
    
    private void updateAltitude(GraphicsContext gc, double altitude){
        
        //Convert Altitude to pixel position
        double posY=(altitude-this.minAlt)*AHRS_ALT_LENGTH/(this.maxAlt-this.minAlt);
        
        //Draw altitude pointer
        AhrsObject pointer=new AhrsObject(new Point2D[]{new Point2D( AHRS_ALT_POS_X+3,AHRS_ALT_POS_Y+AHRS_ALT_LENGTH-posY), 
            new Point2D(AHRS_ALT_POS_X+4+AHRS_RP_XW,AHRS_ALT_POS_Y+AHRS_ALT_LENGTH-AHRS_RP_XW/2-posY),
            new Point2D(AHRS_ALT_POS_X+4+AHRS_RP_XW,AHRS_ALT_POS_Y+AHRS_ALT_LENGTH+AHRS_RP_XW/2-posY)});
        gc.setFill(Color.BLACK);
        Point2D[] hp=pointer.rotate(0, gc.getCanvas().getWidth()/2, gc.getCanvas().getHeight()/2);
        gc.fillPolygon(pointer.getXarray(hp), pointer.getYarray(hp),pointer.getSize());
        
        //Display Altitude text
        String as = String.format("%.0f", altitude);
        drawTextWindow(gc, as, AHRS_ALT_POS_X -60,AHRS_ALT_POS_Y+AHRS_ALT_LENGTH-posY-10 , 45, 20);
    
    } 

    private void updateFltData(GraphicsContext gc, double heading,double airspeed, double altitude) {
        
        Platform.runLater(new Runnable(){
        
            @Override 
            public void run() {
                gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                updateHeading(gc,heading);
                updateSpeed(gc,airspeed);
                updateAltitude(gc,altitude);
            }
        });
        
    }
    
    
    
   
    
    private class HeadingDisplay {
    
    CircularLinkList headingList;
    
    public HeadingDisplay(){
        this.headingList=new CircularLinkList();
        createHeadingList(headingList);
    }
    
    private void createHeadingList(CircularLinkList list){
    
        for(int i=0;i<36;i++){
            headingList.add(i*10);
        }
        
    
        
    }
    public String[] getHeadingLabels(double heading, int num_labels){
        
        int index=((int)heading)/10;
        return headingList.getHead(index,num_labels);
    }
    
    private class CircularLinkList {
    
    private Node head=null;
    private Node tail=null;
    private Node prev=null;
        
    public void add(int value){
        Node newNode=new Node(value);
        if(head==null){
            head=newNode;
            prev=head;
        }else{
            prev=tail;
            tail.nextNode=newNode;
        }
        tail=newNode;
        head.prevNode=tail;
        tail.nextNode=head;
        tail.prevNode=prev;
    }
    
    public String[] getHead(int index,int num_labels){
        Node node=head;
        String[] list=new String[num_labels];
        for(int i=0;i<index;i++){
            node=node.nextNode;
        }
        Node start=node.prevNode;
        start=start.prevNode;
        start=start.prevNode;
        
        for(int j=0;j<num_labels;j++){
            list[j]=start.txt;
            start=start.nextNode;
        }
        
        return list;
    }
    
    
    
    private class Node{
        
        String txt;
        Node nextNode=null;
        Node prevNode=null;
        
        public Node(int value){
            switch(value){
            
                 case 0:
                    this.txt="N";
                    break;
                case 90:
                    this.txt="E";
                    break;
                case 180:
                    this.txt="S";
                    break;
                case 270:
                    this.txt="W";
                    break;
                default:
                    this.txt=String.format("%03d", value);
            }
            
        }
    }
}
}
    private ArrayList<TextNotes> create_msg_list(ArrayList<TextNotes> list, int num_msg){
        
        for(int i=0;i<num_msg;i++){
            list.add(new TextNotes(0,0));
        }
        return list;
    }
    
     private class TextNotes implements Runnable {

        private final int BLINK_TIMER = 500;
        private volatile int blinkState = 0;
        private double posX;
        private double posY;
        private volatile Paint color = Color.BLACK;
        private volatile Paint last_color = color;
        private volatile int fontSize = 12;
        private volatile String label = "";
        private volatile String text = "";
        private Thread blk;
        private volatile boolean running = false;

        public TextNotes(double posX, double posY){
            this.posX=posX;
            this.posY=posY;
        }
        
        public void setPosXY(double posX, double posY) {
            this.posX = posX;
            this.posY=posY;
        }

         public synchronized void setColor(Paint color) {
             if (!running) {
                 this.color = color;
                 this.last_color = color;
             } else {
                 this.last_color = color;
             }
         }

        public synchronized void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

        public synchronized void setLabel(String label) {
            this.label = label;
        }

        public synchronized void setText(String text) {
            this.text = text;
        }

        public void startBlink() {
            if(!running){
            blk = new Thread(this);
            blk.setDaemon(true);
           // running = true;
            blinkState = 1;
            blk.start();
            }
        }

        public synchronized void stopBlink() {

            running = false;

        }

        @Override
        public void run() {
            running = true;
            while (running) {
                switch (blinkState) {

                    case 1:
                        blinkState = 2;
                        this.color = Color.TRANSPARENT;
                        break;

                    case 2:
                        blinkState = 1;
                        this.color = this.last_color;
                        break;

                }
                try {
                    Thread.sleep(BLINK_TIMER);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            blinkState = 0;
            this.color = this.last_color;
        }

    }
    
    private class FlightTimer implements Runnable{
        
        Thread timer;
        private long flightTime;
        public StringProperty elapse_time;
        private volatile boolean running;
        
        public FlightTimer(StringProperty elapse_time){
            this.elapse_time=elapse_time;
        }
        
        public void startTimer(){
            
            flightTime=0;
            running=true;
            timer=new Thread(this);
            timer.setDaemon(true);
            timer.start();
        }

        public void stopTimer(){
            running=false;
        }
        
        public void resetTimer(){
            flightTime=0;
        }
        
        @Override
        public void run() {
            
            while(running){
                elapse_time.set(convertToString(flightTime));
                flightTime++;
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
            
        }
        
        private String convertToString(long t){
    
        double trm=((double)t);
        double time=trm/3600;
        //get Hours
        BigDecimal hours=new BigDecimal(String.valueOf(time));
        int h=hours.intValue();
        
        //Get minutes
        double min=Double.parseDouble(hours.subtract(new BigDecimal(h)).toPlainString())*60;
        BigDecimal minutes=new BigDecimal(String.valueOf(min));
        int m=minutes.intValue();
        
        //Get seconds
        double sec=Double.parseDouble(minutes.subtract(new BigDecimal(m)).toPlainString())*60;
        BigDecimal seconds=new BigDecimal(String.valueOf(sec));
        int s=seconds.intValue();
        
        String str=String.format("%02d:%02d:%02d", h,m,s);
        return str;
        
        
    }
    
    }
        
       
}

