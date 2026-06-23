package uni.client;

import java.io.*;
import java.net.Socket;
import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

public class AIClient {

    private String serverHost;
    private int serverPort;
    private int clientId;

    public AIClient(String host, int port, int id) {
        this.serverHost = host;
        this.serverPort = port;
        this.clientId = id;
    }

    // Sobrecarga para Texto (Payload String)
    public String sendTask(byte nodeType, String payload) {
        return sendTask(nodeType, payload.getBytes());
    }

    // El método que realmente hace el trabajo (Payload byte[])
    public String sendTask(byte nodeType, byte[] binaryPayload) {
        ProtocolMessage request = new ProtocolMessage(
            nodeType,
            clientId,
            binaryPayload
        );
        byte[] requestBytes = MessageCodec.encode(request);

        try (
            Socket socket = new Socket(serverHost, serverPort);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream()
        ) {
            // 1. Enviar la petición
            out.write(requestBytes);
            out.flush();

            // 2. Leer la cabecera de forma segura (9 bytes)
            byte[] headerBuffer = new byte[MessageCodec.HEADER_SIZE];
            int headerRead = 0;
            while (headerRead < MessageCodec.HEADER_SIZE) {
                int read = in.read(headerBuffer, headerRead, MessageCodec.HEADER_SIZE - headerRead);
                if (read == -1) throw new Exception("Conexión cerrada por el servidor al leer cabecera.");
                headerRead += read;
            }
            
            ProtocolMessage resHeader = MessageCodec.decodeHeader(headerBuffer);

            // 3. Leer el payload de forma segura (Ciclo While para evitar fragmentación TCP)
            byte[] payloadBuffer = new byte[resHeader.getPayloadLength()];
            int payloadRead = 0;
            while (payloadRead < payloadBuffer.length) {
                int read = in.read(payloadBuffer, payloadRead, payloadBuffer.length - payloadRead);
                if (read == -1) throw new Exception("Fin de flujo inesperado al leer el payload.");
                payloadRead += read;
            }

            return new String(payloadBuffer);
            
        } catch (Exception e) {
            return "Error de Red: " + e.getMessage();
        }
    }
}