package uni.client;

import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AIClient {
    private final String serverHost;
    private final int serverPort;
    private final int clientId;

    public AIClient(String serverHost, int serverPort, int clientId) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientId = clientId;
    }

    public String sendTask(byte nodeType, String textPayload) {
        ProtocolMessage request = new ProtocolMessage(nodeType, clientId, textPayload.getBytes());
        byte[] requestBytes = MessageCodec.encode(request);

        try (Socket socket = new Socket(serverHost, serverPort);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            // Enviar petición
            out.write(requestBytes);
            out.flush();

            // Leer respuesta (Cabecera)
            byte[] headerBuffer = new byte[MessageCodec.HEADER_SIZE];
            in.read(headerBuffer);
            ProtocolMessage partialResponse = MessageCodec.decodeHeader(headerBuffer);

            // Leer respuesta (Payload)
            byte[] payloadBuffer = new byte[partialResponse.getPayloadLength()];
            int payloadRead = 0;
            while (payloadRead < payloadBuffer.length) {
                payloadRead += in.read(payloadBuffer, payloadRead, payloadBuffer.length - payloadRead);
            }

            return new String(payloadBuffer);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error de conexión: " + e.getMessage();
        }
    }
}