package uni.server;

import uni.protocol.ProtocolMessage;

public class MessageRouter {
    
    // Constantes para identificar el tipo de nodo objetivo
    public static final byte NODE_TEXT = 0x01;
    public static final byte NODE_IMAGE = 0x02;

    private final NodeDispatcher dispatcher;

    public MessageRouter() {
        // Instanciamos el despachador que abrirá los sockets locales hacia los nodos
        this.dispatcher = new NodeDispatcher();
    }

    /**
     * Procesa el mensaje entrante basado en el ID y tipo de nodo.
     * Ahora delega el procesamiento real a los nodos a través de la red interna.
     */
    public ProtocolMessage route(ProtocolMessage incomingMessage) {
        int clientId = incomingMessage.getClientId();
        byte targetNode = incomingMessage.getNodeType();
        
        System.out.println("[Router] Enrutando petición del Cliente ID: " + clientId + " hacia el nodo: " + targetNode);

        try {
            // Pasamos el mensaje al dispatcher para que viaje por la red hasta el TaskProcessor
            return dispatcher.dispatch(incomingMessage);
        } catch (Exception e) {
            System.err.println("[Router] Error de red al comunicarse con el nodo: " + e.getMessage());
            
            // Si el nodo está caído o la red falla, devolvemos un mensaje de error al cliente
            String errorMsg = "Error 500: Fallo de comunicación con el nodo - " + e.getMessage();
            return new ProtocolMessage(targetNode, clientId, errorMsg.getBytes());
        }
    }
}