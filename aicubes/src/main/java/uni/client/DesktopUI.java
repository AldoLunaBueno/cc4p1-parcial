package uni.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uni.server.MessageRouter;

import java.io.File;

public class DesktopUI extends Application {

    private AIClient aiClient;
    private TextArea chatArea;
    private TextField inputField;
    private TextField ipServerField;
    private Button connectBtn;
    private Button sendTextBtn;
    private Button sendImageBtn;
    private Label statusLabel;

    @Override
    public void init() {
        // Inicializamos el cliente (Host, Puerto, ID de Cliente)
        // En un escenario real, esto podría venir de un login
        this.aiClient = new AIClient("127.0.0.1", 8080, 812);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Open AI Cube - Cliente Distribuido");

        // --- 1. Barra de Conexión ---
        ipServerField = new TextField("127.0.0.1");
        ipServerField.setPromptText("IP Servidor Central");
        connectBtn = new Button("Conectar");
        
        HBox connectionBox = new HBox(10, new Label("Host:"), ipServerField, connectBtn);
        connectionBox.setPadding(new Insets(10));
        connectionBox.setStyle("-fx-background-color: #e0e0e0;");

        // --- 2. Área de Chat ---
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // --- 3. Controles de Mensaje ---
        inputField = new TextField();
        inputField.setPromptText("Ingrese consulta...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        // 3. Botones de acción
        sendTextBtn = new Button("Enviar Texto");
        sendImageBtn = new Button("Enviar Imagen");
        
        // Bloqueo inicial según requerimiento
        setControlsEnabled(false);

        HBox actionBox = new HBox(10, inputField, sendTextBtn, sendImageBtn);
        statusLabel = new Label("Estado: Desconectado");

        // --- Lógica de Conexión ---
        connectBtn.setOnAction(e -> {
            String host = ipServerField.getText().trim();
            if (!host.isEmpty()) {
                this.aiClient = new AIClient(host, 8080, 812);
                setControlsEnabled(true);
                statusLabel.setText("Estado: Conectado a " + host);
                appendMessage("SISTEMA: Conexión establecida con el Cubo Central.");
            }
        });

        sendTextBtn.setOnAction(e -> handleSendText());
        sendImageBtn.setOnAction(e -> handleSendImage(primaryStage));

        VBox root = new VBox(connectionBox, chatArea, actionBox, statusLabel);
        root.setPadding(new Insets(10));
        
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    private void setControlsEnabled(boolean enabled) {
        sendTextBtn.setDisable(!enabled);
        sendImageBtn.setDisable(!enabled);
        inputField.setDisable(!enabled);
    }

    private void handleSendText() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        appendMessage("Yo (Texto): " + text);
        inputField.clear();
        processTask(MessageRouter.NODE_TEXT, text);
    }

    private void handleSendImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen para el Cubo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            appendMessage("Yo (Imagen): " + file.getName());
            // En este sprint enviamos el nombre o un placeholder como payload
            processTask(MessageRouter.NODE_IMAGE, "Procesando archivo: " + file.getName());
        }
    }

    /**
     * Ejecuta la tarea en un hilo separado para no congelar la UI de la UNI.
     */
    private void processTask(byte nodeType, String payload) {
        setLoading(true);
        
        new Thread(() -> {
            // Llamada bloqueante al socket (Capa de Red)
            String response = aiClient.sendTask(nodeType, payload);

            // Regresamos al hilo de UI para mostrar el resultado
            Platform.runLater(() -> {
                appendMessage("ChatCube: " + response);
                setLoading(false);
            });
        }).start();
    }

    private void appendMessage(String msg) {
        chatArea.appendText(msg + "\n\n");
    }

    private void setLoading(boolean loading) {
        statusLabel.setText(loading ? "Estado: Procesando en Nodo..." : "Estado: Listo");
        sendTextBtn.setDisable(loading);
        sendImageBtn.setDisable(loading);
        inputField.setDisable(loading);
    }

    public static void main(String[] args) {
        launch(args);
    }
}