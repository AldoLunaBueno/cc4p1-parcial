module uni.aicubes {
    // Usamos transitive para exponer JavaFX a quienes consuman nuestra UI
    requires transitive javafx.controls;
    requires transitive javafx.graphics;

    // Exportamos los paquetes que realmente contienen tus clases
    exports uni.client;
    exports uni.server;
    exports uni.module;
    exports uni.protocol;
}