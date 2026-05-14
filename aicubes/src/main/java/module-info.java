module uni.aicubes {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;

    // Permite que JavaFX acceda a tu UI para iniciar la App
    opens uni.client to javafx.graphics, javafx.fxml;

    exports uni.client;
    exports uni.server;
    exports uni.module;
    exports uni.protocol;
}
