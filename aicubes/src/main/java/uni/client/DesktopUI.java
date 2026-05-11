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
        primaryStage.setTitle("Open AI Cube - UNI CC4P1");

        // 1. Área de Chat (Historial)
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // 2. Campo de entrada
        inputField = new TextField();
        inputField.setPromptText("Escribe tu consulta de texto aquí...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        // 3. Botones de acción
        sendTextBtn = new Button("Enviar Texto");
        sendImageBtn = new Button("Enviar Imagen");
        
        statusLabel = new Label("Estado: Listo");

        HBox actionBox = new HBox(10, inputField, sendTextBtn, sendImageBtn);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        // Layout Principal
        VBox root = new VBox(10, chatArea, actionBox, statusLabel);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f4f4f4;");

        // --- Eventos ---

        sendTextBtn.setOnAction(e -> handleSendText());
        
        inputField.setOnAction(e -> handleSendText());

        sendImageBtn.setOnAction(e -> handleSendImage(primaryStage));

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
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