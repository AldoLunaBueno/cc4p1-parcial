package uni.integration;

// import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uni.client.AIClient;
import uni.module.ImageDummyProcessor;
import uni.module.NodeServer;
import uni.module.TextDummyProcessor;
import uni.server.CentralServer;
import uni.server.MessageRouter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SystemIntegrationTest {

    private static final int CENTRAL_PORT = 8080;
    private static final int TEXT_NODE_PORT = 9001;
    private static final int IMAGE_NODE_PORT = 9002;
    private static final String HOST = "127.0.0.1";

    @BeforeEach
    public void setupEnvironment() throws InterruptedException {
        System.out.println("Levantando infraestructura de prueba...");

        // 1. Iniciar Nodo de Texto en un hilo en segundo plano
        new Thread(() -> {
            new NodeServer(TEXT_NODE_PORT, new TextDummyProcessor()).start();
        }).start();

        // 2. Iniciar Nodo de Imagen en un hilo en segundo plano
        new Thread(() -> {
            new NodeServer(IMAGE_NODE_PORT, new ImageDummyProcessor()).start();
        }).start();

        // 3. Iniciar Servidor Central en un hilo en segundo plano
        new Thread(() -> {
            new CentralServer(CENTRAL_PORT).start();
        }).start();

        // Dar tiempo a que los ServerSockets se inicialicen correctamente (1.5 segundos)
        Thread.sleep(1500);
        System.out.println("Infraestructura lista. Iniciando pruebas...\n");
    }

    @Test
    public void testTextNodeRoutingAndProcessing() {
        // Arrange: Crear un cliente con ID 101
        AIClient client = new AIClient(HOST, CENTRAL_PORT, 101);
        String testPayload = "Hola, necesito procesar este texto.";

        // Act: Enviar la tarea indicando que es para el nodo de texto
        long startTime = System.currentTimeMillis();
        String response = client.sendTask(MessageRouter.NODE_TEXT, testPayload);
        long duration = System.currentTimeMillis() - startTime;

        // Assert: Verificar la respuesta y el retardo emulado
        assertNotNull(response, "La respuesta no debería ser nula");
        assertTrue(response.contains("Texto procesado exitosamente"), "La respuesta no proviene del TextProcessor");
        assertTrue(response.contains(testPayload), "La respuesta no contiene el payload original modificado");
        
        // Verifica que el hilo durmió los 1.5s que le pusimos al TextDummyProcessor
        assertTrue(duration >= 1500, "El tiempo de procesamiento fue menor al emulado por el micro chunk");
        
        System.out.println("Prueba de Texto Exitosa. Tiempo: " + duration + "ms. Respuesta: " + response);
    }

    @Test
    public void testImageNodeRoutingAndProcessing() {
        // Arrange: Crear un cliente con ID 202
        AIClient client = new AIClient(HOST, CENTRAL_PORT, 202);
        String testPayload = "Fake_Image_Bytes";

        // Act: Enviar la tarea indicando que es para el nodo de imagen
        long startTime = System.currentTimeMillis();
        String response = client.sendTask(MessageRouter.NODE_IMAGE, testPayload);
        long duration = System.currentTimeMillis() - startTime;

        // Assert: Verificar la respuesta y el retardo emulado
        assertNotNull(response, "La respuesta no debería ser nula");
        assertTrue(response.contains("Imagen procesada exitosamente"), "La respuesta no proviene del ImageProcessor");
        
        // Verifica que el hilo durmió los 2.0s que le pusimos al ImageDummyProcessor
        assertTrue(duration >= 2000, "El tiempo de procesamiento fue menor al emulado por el micro chunk");

        System.out.println("Prueba de Imagen Exitosa. Tiempo: " + duration + "ms. Respuesta: " + response);
    }
}