package uni.server;

import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NodeDispatcher {
    
    private final String textNodeHost;
    private final int textNodePort;
    private final String imageNodeHost;
    private final int imageNodePort;

    /**
     * Constructor por defecto (Retrocompatibilidad Sprint 1 y 2).
     * Apunta todo a localhost.
     */
    public NodeDispatcher() {
        this("127.0.0.1", 9001, "127.0.0.1", 9002);
    }

    /**
     * Constructor distribuido (Sprint 3 y 4).
     */
    public NodeDispatcher(String textHost, int textPort, String imageHost, int imagePort) {
        this.textNodeHost = textHost;
        this.textNodePort = textPort;
        this.imageNodeHost = imageHost;
        this.imageNodePort = imagePort;
    }

    public ProtocolMessage dispatch(ProtocolMessage request) throws Exception {
        String targetHost;
        int targetPort;
        
        if (request.getNodeType() == MessageRouter.NODE_TEXT) {
            targetHost = textNodeHost;
            targetPort = textNodePort;
        } else if (request.getNodeType() == MessageRouter.NODE_IMAGE) {
            targetHost = imageNodeHost;
            targetPort = imageNodePort;
        } else {
            throw new IllegalArgumentException("Tipo de nodo desconocido: " + request.getNodeType());
        }

        System.out.println("[Dispatcher] Conectando a IP " + targetHost + " en el puerto " + targetPort + "...");

        try (Socket nodeSocket = new Socket(targetHost, targetPort);
             OutputStream out = nodeSocket.getOutputStream();
             InputStream in = nodeSocket.getInputStream()) {

            byte[] requestBytes = MessageCodec.encode(request);
            out.write(requestBytes);
            out.flush();

            byte[] headerBuffer = new byte[MessageCodec.HEADER_SIZE];
            int bytesRead = in.read(headerBuffer);
            if (bytesRead < MessageCodec.HEADER_SIZE) throw new Exception("Respuesta del nodo incompleta.");

            ProtocolMessage partialResponse = MessageCodec.decodeHeader(headerBuffer);

            byte[] payloadBuffer = new byte[partialResponse.getPayloadLength()];
            int payloadRead = 0;
            while (payloadRead < payloadBuffer.length) {
                int read = in.read(payloadBuffer, payloadRead, payloadBuffer.length - payloadRead);
                if (read == -1) throw new Exception("Fin de flujo inesperado.");
                payloadRead += read;
            }

            return new ProtocolMessage(partialResponse.getNodeType(), partialResponse.getClientId(), payloadBuffer);
        }
    }
}