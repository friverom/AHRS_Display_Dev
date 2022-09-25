package com.frm.ahrsdisplay1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    
    @Override
    public void start(Stage stage) throws IOException {
        
        FXMLLoader fxmlloader=new FXMLLoader();
        Parent root=fxmlloader.load(getClass().getResource("MainWindowFXML.fxml").openStream());
        MainWindowFXMLController controller1=(MainWindowFXMLController)fxmlloader.getController();
        
        Scene scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        stage.setOnCloseRequest(e->{
            controller1.exitApp();
        });
        
    }

    

    public static void main(String[] args) {
        launch();
    }

}