module tcpklient.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires javafx.graphics;
    requires java.desktop;
    requires com.google.gson;

    opens main to javafx.fxml;
    exports main;
    exports main.controllers;
    opens main.controllers to javafx.fxml;
    exports main.helpers;
    opens main.helpers to javafx.fxml;
    exports main.messageTypes;
    opens main.messageTypes to javafx.fxml;
    exports main.managers.settings;
    exports main.managers.console;
    opens main.managers.console to javafx.fxml;
    exports main.helpers.Listeners;
    opens main.helpers.Listeners to javafx.fxml;
}
