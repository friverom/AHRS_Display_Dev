module com.frm.ahrsdisplay1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires com.fazecast.jSerialComm;

    opens com.frm.ahrsdisplay1 to javafx.fxml;
    exports com.frm.ahrsdisplay1;
}
