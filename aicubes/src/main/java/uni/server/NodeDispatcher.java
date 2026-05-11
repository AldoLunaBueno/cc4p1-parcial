package uni.server;

import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NodeDispatcher {
    
    // Puertos fijos para el entorno local del Sprint 1
    private static final String HOST = "127.0.0.1";
    private static final int PORT_TEXT_NODE = 9001;
    private static final int PORT_IMAGE_NODE = 9002;

    public ProtocolMessage dispatch(ProtocolMessage request) throws Exception {
        int targetPort;
        
        // Determinar el puerto basado en el tipo de nodo
        if (request.getNodeType() == MessageRouter.NODE_TEXT) {
            targetPort = PORT_TEXT_NODE;
        } else if (request.getNodeType() == MessageRouter.NODE_IMAGE) {
            targetPort = PORT_IMAGE_NODE;
        } else {
            throw new IllegalArgumentException("Tipo de nodo desconocido: " + request.getNodeType());
        }

        System.out.println("[Dispatcher] Conectando al nodo en el puerto " + targetPort + "...");

        // Usamos try-with-resources para garantizar el cierre del socket
        try (Socket nodeSocket = new Socket(HOST, targetPort);
             OutputStream out = nodeSocket.getOutputStream();
             InputStream in = nodeSocket.getInputStream()) {

            // 1. Enviar la petición al Nodo
            byte[] requestBytes = MessageCodec.encode(request);
            out.write(requestBytes);
            out.flush();

            // 2. Leer la respuesta del Nodo (Cabecera)
            byte[] headerBuffer = new byte[MessageCodec.HEADER_SIZE];
            int bytesRead = in.read(headerBuffer);
            if (bytesRead < MessageCodec.HEADER_SIZE) {
                throw new Exception("Respuesta del nodo incompleta.");
            }

            ProtocolMessage partialResponse = MessageCodec.decodeHeader(headerBuffer);

            // 3. Leer la respuesta del Nodo (Payload)
            byte[] payloadBuffer = new byte[partialResponse.getPayloadLength()];
            int payloadRead = 0;
            while (payloadRead < payloadBuffer.length) {
                int read = in.read(payloadBuffer, payloadRead, payloadBuffer.length - payloadRead);
                if (read == -1) throw new Exception("Fin de flujo inesperado desde el nodo.");
                payloadRead += read;
            }

            // 4. Retornar el mensaje ensamblado al Router
            return new ProtocolMessage(
                partialResponse.getNodeType(), 
                partialResponse.getClientId(), 
                payloadBuffer
            );
        }
    }
}