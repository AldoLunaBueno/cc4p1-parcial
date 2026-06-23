package uni.client;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uni.server.MessageRouter;

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
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Open AI Cube - Cliente Distribuido");

        // --- 1. Barra de Conexión (Nuestro diseño distribuido) ---
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
        inputField.setPromptText("Escribe tu consulta de texto aquí...");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        
        sendTextBtn = new Button("Enviar Texto");
        sendImageBtn = new Button("Enviar Imagen");
        
        // Bloqueo inicial hasta que haya conexión
        setControlsEnabled(false);

        HBox actionBox = new HBox(10, inputField, sendTextBtn, sendImageBtn);
        actionBox.setPadding(new Insets(10, 0, 0, 0));
        
        statusLabel = new Label("Estado: Desconectado");

        // --- Lógica de Interfaz ---
        connectBtn.setOnAction(e -> {
            String host = ipServerField.getText().trim();
            if (!host.isEmpty()) {
                int clientId = (int) (Math.random() * 1000);
                this.aiClient = new AIClient(host, 8080, clientId);
                setControlsEnabled(true);
                statusLabel.setText("Estado: Conectado a " + host);
                appendMessage("SISTEMA: Conexión establecida con el Cubo Central.");
            }
        });

        sendTextBtn.setOnAction(e -> handleSendText());
        inputField.setOnAction(e -> handleSendText());
        sendImageBtn.setOnAction(e -> handleSendImage(primaryStage));

        VBox root = new VBox(10, connectionBox, chatArea, actionBox, statusLabel);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f4f4f4;");
        
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
        // Enviamos el texto convirtiéndolo a bytes
        processTask(MessageRouter.NODE_TEXT, text.getBytes());
    }

    private void handleSendImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen para el Cubo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            appendMessage("Yo (Imagen): Procesando archivo " + file.getName() + "...");

            try {
                // Usamos la excelente lógica matemática de tu compañero
                byte[] pixeles = transformarImagen(file);
                processTask(MessageRouter.NODE_IMAGE, pixeles);
            } catch (Exception e) {
                appendMessage("Error procesando imagen: " + e.getMessage());
            }
        }
    }

    // --- ALGORITMO DE TRANSFORMACIÓN DE IMAGEN (Conservado intacto) ---
    private byte[] transformarImagen(File file) throws Exception {
        BufferedImage original = ImageIO.read(file);

        Image scaled = original.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        byte[] pixeles = new byte[784];
        double sumaCheck = 0;

        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                sumaCheck += (resized.getRGB(x, y) & 0xFF);
            }
        }
        boolean debeInvertir = (sumaCheck / 784.0) > 127;

        double sumaFinal = 0;
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int gray = (resized.getRGB(x, y) & 0xFF);
                if (debeInvertir) gray = 255 - gray;
                if (gray < 140) gray = 0;

                pixeles[y * 28 + x] = (byte) gray;
                sumaFinal += (gray / 255.0);
            }
        }

        System.out.println("[DEBUG Java] Suma normalizada calculada: " + sumaFinal);
        return pixeles;
    }

    /**
     * Unificamos el procesamiento en un solo hilo asíncrono para Textos y Bytes puros.
     */
    private void processTask(byte nodeType, byte[] payload) {
        setLoading(true);
        new Thread(() -> {
            // Nota: Asegúrate de que aiClient.sendTask esté sobrecargado para aceptar byte[]
            String response = aiClient.sendTask(nodeType, payload);

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
        statusLabel.setText(loading ? "Estado: Procesando en la red..." : "Estado: Conectado y Listo");
        setControlsEnabled(!loading);
    }

    public static void main(String[] args) {
        launch(args);
    }
}