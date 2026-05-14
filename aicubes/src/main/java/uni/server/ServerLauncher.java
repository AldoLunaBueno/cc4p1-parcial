package uni.server;

import uni.module.ImageDummyProcessor;
import uni.module.NodeServer;
import uni.module.TextProcessor;

public class ServerLauncher {
    public static void main(String[] args) {
        System.out.println("Iniciando infraestructura Open AI Cubes (Sprint 2 - Paralelismo)...");

        // 1. Levantar el Nodo de Texto con el procesador matemático concurrente
        new Thread(() -> {
            // Pasamos nuestro nuevo TextProcessor que maneja los hilos internamente
            new NodeServer(9001, new TextProcessor()).start();
        }).start();

        // 2. Levantar el Nodo de Imagen (Aún en versión Dummy para este sprint)
        new Thread(() -> {
            new NodeServer(9002, new ImageDummyProcessor()).start();
        }).start();

        // Damos 1 segundo para asegurar que los puertos 9001 y 9002 estén listos
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Levantar el Servidor Central multihilo
        System.out.println("Iniciando enrutador principal multihilo...");
        new CentralServer(8080).start();
    }
}