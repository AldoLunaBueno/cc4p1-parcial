package uni.server;

import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralServer {
    private final int port;
    private final MessageRouter router;

    public CentralServer(int port) {
        this.port = port;
        this.router = new MessageRouter();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor Central iniciado en el puerto: " + port);
            
            // Ciclo secuencial (Sprint 1)
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleConnection(clientSocket);
                } catch (Exception e) {
                    System.err.println("Error procesando conexión: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        ProtocolMessage partialMessage = MessageCodec.decodeHeader(headerBuffer);

        // 2. Leer la carga útil (Payload)
        byte[] payloadBuffer = new byte[partialMessage.getPayloadLength()];
        int payloadRead = 0;
        while (payloadRead < payloadBuffer.length) {
            int read = in.read(payloadBuffer, payloadRead, payloadBuffer.length - payloadRead);
            if (read == -1) throw new Exception("Fin de flujo inesperado leyendo payload.");
            payloadRead += read;
        }

        // 3. Reconstruir el mensaje completo
        ProtocolMessage fullRequest = new ProtocolMessage(
            partialMessage.getNodeType(), 
            partialMessage.getClientId(), 
            payloadBuffer
        );

        // 4. Delegar a la capa de dominio (Router)
        ProtocolMessage response = router.route(fullRequest);

        // 5. Codificar y enviar respuesta
        byte[] responseBytes = MessageCodec.encode(response);
        out.write(responseBytes);
        out.flush();
    }
}