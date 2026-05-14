package uni.client;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils; // Para convertir entre JavaFX y Swing
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import uni.server.MessageRouter;

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
        // ... tu configuración de fileChooser ...

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            appendMessage("Yo (Imagen): " + file.getName());

            try {
                // 1. Convertir archivo a 784 bytes (28x28 escala de grises)
                byte[] pixeles = transformarImagen(file);

                // 2. Enviar los bytes reales, NO el nombre del archivo
                setLoading(true);
                new Thread(() -> {
                    // Aquí usamos una versión de sendTask que acepte byte[]
                    String response = aiClient.sendTask(
                        MessageRouter.NODE_IMAGE,
                        pixeles
                    );

                    Platform.runLater(() -> {
                        appendMessage("ChatCube: " + response);
                        setLoading(false);
                    });
                })
                    .start();
            } catch (Exception e) {
                appendMessage("Error procesando imagen: " + e.getMessage());
            }
        }
    }

    private byte[] transformarImagen(File file) throws Exception {
        BufferedImage original = ImageIO.read(file);

        // 1. Redimensionar a 28x28 y convertir a escala de grises
        Image scaled = original.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(
            28,
            28,
            BufferedImage.TYPE_BYTE_GRAY
        );
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        byte[] pixeles = new byte[784];
        double sumaCheck = 0;

        // Primer pase: calcular brillo promedio para decidir si invertir
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                sumaCheck += (resized.getRGB(x, y) & 0xFF);
            }
        }
        boolean debeInvertir = (sumaCheck / 784.0) > 127; // Si el promedio es claro, invertimos

        double sumaFinal = 0;
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int gray = (resized.getRGB(x, y) & 0xFF);

                if (debeInvertir) {
                    gray = 255 - gray;
                }

                // --- BINARIZACIÓN (LIMPIEZA TOTAL) ---
                // Si el pixel no es suficientemente brillante, lo hacemos negro puro (0)
                // Si es brillante, lo dejamos como está o lo hacemos blanco puro (255)
                if (gray < 140) {
                    gray = 0;
                }

                pixeles[y * 28 + x] = (byte) gray;
                sumaFinal += (gray / 255.0);
            }
        }

        System.out.println(
            "[DEBUG Java] Suma normalizada calculada: " + sumaFinal
        );
        return pixeles;
    }

    /**
     * Ejecuta la tarea en un hilo separado para no congelar la UI de la UNI.
     */
    private void processTask(byte nodeType, String payload) {
        setLoading(true);
        new Thread(() -> {
            // AIClient ya sabe cómo manejar el Socket y el ProtocolMessage
            String response = aiClient.sendTask(nodeType, payload);

            Platform.runLater(() -> {
                appendMessage("ChatCube: " + response);
                setLoading(false);
            });
        })
            .start();
    }

    private void appendMessage(String msg) {
        chatArea.appendText(msg + "\n\n");
    }

    private void setLoading(boolean loading) {
        statusLabel.setText(
            loading ? "Estado: Procesando en Nodo..." : "Estado: Listo"
        );
        sendTextBtn.setDisable(loading);
        sendImageBtn.setDisable(loading);
        inputField.setDisable(loading);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
