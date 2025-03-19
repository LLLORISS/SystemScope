module nm.sc.systemscope {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires com.github.oshi;
    requires java.desktop;
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires annotations;

    opens nm.sc.systemscope to javafx.fxml;
    exports nm.sc.systemscope;
    exports nm.sc.systemscope.controllers;
    opens nm.sc.systemscope.controllers to javafx.fxml;
    exports nm.sc.systemscope.modules;
    opens nm.sc.systemscope.modules to javafx.fxml;
    exports nm.sc.systemscope.adapters;
    opens nm.sc.systemscope.adapters to javafx.fxml;
}