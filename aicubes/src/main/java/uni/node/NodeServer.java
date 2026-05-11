package uni.node;

import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeServer {
    private final int port;
    private final TaskProcessor processor;

    public NodeServer(int port, TaskProcessor processor) {
        this.port = port;
        this.processor = processor;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Nodo iniciado y escuchando en el puerto: " + port);
            
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleConnection(clientSocket);
                } catch (Exception e) {
                    System.err.println("Error procesando tarea: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) throws Exception {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        // 1. Leer petición
        byte[] headerBuffer = new byte[MessageCodec.HEADER_SIZE];
        if (in.read(headerBuffer) < MessageCodec.HEADER_SIZE) return;
        
        ProtocolMessage requestHeader = MessageCodec.decodeHeader(headerBuffer);
        
        byte[] requestPayload = new byte[requestHeader.getPayloadLength()];
        int payloadRead = 0;
        while (payloadRead < requestPayload.length) {
            payloadRead += in.read(requestPayload, payloadRead, requestPayload.length - payloadRead);
        }

        // 2. Procesar la tarea utilizando la estrategia inyectada
        byte[] resultPayload = processor.process(requestPayload);

        // 3. Empaquetar y enviar respuesta
        ProtocolMessage responseMessage = new ProtocolMessage(
            requestHeader.getNodeType(), 
            requestHeader.getClientId(), 
            resultPayload
        );
        
        out.write(MessageCodec.encode(responseMessage));
        out.flush();
    }
}