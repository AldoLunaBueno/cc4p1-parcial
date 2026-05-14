package uni.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

public class CentralServer {

    private final int port;
    private final MessageRouter router;
    private final ExecutorService threadPool;

    public CentralServer(int port, int maxThreads) {
        this.port = port;
        this.router = new MessageRouter();

        // Controlamos el paralelismo del servidor proxy
        if (maxThreads <= 1) {
            this.threadPool = Executors.newSingleThreadExecutor();
        } else {
            // newCachedThreadPool es ideal para red, pero limitamos conceptualmente
            // con maxThreads si quisiéramos un FixedThreadPool. Para I/O Cached es mejor.
            this.threadPool = Executors.newCachedThreadPool();
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(
                "Servidor Central iniciado en el puerto: " + port
            );

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // Delegamos la conexión a un hilo del pool para no bloquear el ciclo
                    threadPool.submit(() -> {
                        try (clientSocket) {
                            // Try-with-resources para el auto-cierre del socket
                            handleConnection(clientSocket);
                        } catch (Exception e) {
                            System.err.println(
                                "Error procesando conexión: " + e.getMessage()
                            );
                        }
                    });
                } catch (Exception e) {
                    System.err.println(
                        "Error aceptando cliente: " + e.getMessage()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) throws Exception {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        // 1. Leer la cabecera (9 bytes fijos)
        byte[] headerBuffer = new byte[MessageCodec.HEADER_SIZE];
        int bytesRead = in.read(headerBuffer);

        if (bytesRead < MessageCodec.HEADER_SIZE) {
            throw new Exception("Cabecera incompleta o conexión cerrada.");
        }

        ProtocolMessage partialMessage = MessageCodec.decodeHeader(
            headerBuffer
        );

        // 2. Leer la carga útil (Payload)
        byte[] payloadBuffer = new byte[partialMessage.getPayloadLength()];
        int payloadRead = 0;
        while (payloadRead < payloadBuffer.length) {
            int read = in.read(
                payloadBuffer,
                payloadRead,
                payloadBuffer.length - payloadRead
            );
            if (read == -1) throw new Exception(
                "Fin de flujo inesperado leyendo payload."
            );
            payloadRead += read;
        }

        // 3. Reconstruir el mensaje completo
        ProtocolMessage fullRequest = new ProtocolMessage(
            partialMessage.getNodeType(),
            partialMessage.getClientId(),
            payloadBuffer
        );

        // 4. Delegar al Router (que usa el Dispatcher)
        ProtocolMessage response = router.route(fullRequest);

        // 5. Enviar al cliente
        byte[] responseBytes = MessageCodec.encode(response);
        out.write(responseBytes);
        out.flush();
    }
}
