module nm.sc.systemscope {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.github.oshi;
    requires java.desktop;
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;

    opens nm.sc.systemscope to javafx.fxml;
    exports nm.sc.systemscope;
    exports nm.sc.systemscope.Controllers;
    opens nm.sc.systemscope.Controllers to javafx.fxml;
}