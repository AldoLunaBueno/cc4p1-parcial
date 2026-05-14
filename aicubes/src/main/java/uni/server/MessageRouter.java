package uni.server;

import uni.protocol.ProtocolMessage;

public class MessageRouter {
    
    // Constantes para identificar el tipo de nodo objetivo
    public static final byte NODE_TEXT = 0x01;
    public static final byte NODE_IMAGE = 0x02;

    private final NodeDispatcher dispatcher;

    /**
     * Constructor por defecto (Retrocompatibilidad).
     * Crea un dispatcher local.
     */
    public MessageRouter() {
        // Instanciamos el despachador que abrirá los sockets locales hacia los nodos
        this.dispatcher = new NodeDispatcher();
    }

    /**
     * Constructor distribuido.
     * Pasa la topología de red al dispatcher.
     */
    public MessageRouter(String textHost, int textPort, String imgHost, int imgPort) {
        this.dispatcher = new NodeDispatcher(textHost, textPort, imgHost, imgPort);
    }

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
    
    // Getters opcionales para imprimir logs de topología en el CentralServer
    public String getTextHost() { return "Configurado en Dispatcher"; }
    public int getTextPort() { return 9001; }
    public String getImgHost() { return "Configurado en Dispatcher"; }
    public int getImgPort() { return 9002; }
}