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
            out.write(requestBytes);
            out.flush();

            // Lógica de lectura de respuesta...
            byte[] headerBuffer = new byte[MessageCodec.HEADER_SIZE];
            in.read(headerBuffer);
            ProtocolMessage resHeader = MessageCodec.decodeHeader(headerBuffer);

            byte[] payloadBuffer = new byte[resHeader.getPayloadLength()];
            in.read(payloadBuffer);

            return new String(payloadBuffer);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
