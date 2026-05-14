package uni.server;

import uni.module.ImageDummyProcessor;
import uni.module.NodeServer;
import uni.module.TextProcessor;

public class ServerLauncher {
    public static void main(String[] args) {
        // Valores por defecto (Modo Paralelo, Integración C++)
        int numThreads = 4;
        boolean launchJavaImageNode = false;

        // Parseo de argumentos por consola (Ej: java uni.server.ServerLauncher 1 true)
        if (args.length >= 1) {
            numThreads = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            launchJavaImageNode = Boolean.parseBoolean(args[1]);
        }

        System.out.println("=====================================================");
        System.out.println("Iniciando infraestructura Open AI Cubes");
        System.out.println("Configuración actual -> Hilos por Nodo: " + numThreads);
        System.out.println("Nodo de Imagen Local (Java): " + (launchJavaImageNode ? "ACTIVADO" : "DESACTIVADO (Esperando C++)"));
        System.out.println("=====================================================\n");

        final int finalNumThreads = numThreads;

        // 1. Levantar el Nodo de Texto (Siempre en Java para este alcance)
        new Thread(() -> {
            new NodeServer(9001, new TextProcessor(finalNumThreads)).start();
        }).start();

        // 2. Levantar el Nodo de Imagen dinámicamente
        if (launchJavaImageNode) {
            new Thread(() -> {
                new NodeServer(9002, new ImageDummyProcessor()).start();
            }).start();
        } else {
            System.out.println("[INFO] El puerto 9002 queda libre para que conectes el nodo C++ (make run).");
        }

        // Damos 1 segundo para asegurar que los puertos estén listos
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Levantar el Servidor Central
        System.out.println("Iniciando enrutador principal...");
        new CentralServer(8080, numThreads).start();
    }
}