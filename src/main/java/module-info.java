module ru.alexander.vtscupcounter {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    requires ru.alexander.jVTSAPI;
    requires org.java_websocket;
    requires com.google.gson;


    opens ru.alexander.vtscupcounter to javafx.fxml;
    exports ru.alexander.vtscupcounter;
}