package uni.server;

import uni.node.ImageDummyProcessor;
import uni.node.NodeServer;
import uni.node.TextDummyProcessor;

public class ServerLauncher {
    public static void main(String[] args) {
        System.out.println("Iniciando infraestructura Open AI Cubes (Sprint 1)...");

        // 1. Levantar el Nodo de Texto (Puerto 9001) en su propio hilo
        new Thread(() -> {
            new NodeServer(9001, new TextDummyProcessor()).start();
        }).start();

        // 2. Levantar el Nodo de Imagen (Puerto 9002) en su propio hilo
        new Thread(() -> {
            new NodeServer(9002, new ImageDummyProcessor()).start();
        }).start();

        // Damos 1 segundo para asegurar que los puertos 9001 y 9002 estén listos
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Levantar el Servidor Central (Puerto 8080) en el hilo principal
        System.out.println("Iniciando enrutador principal...");
        new CentralServer(8080).start();
    }
}