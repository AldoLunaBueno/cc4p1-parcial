package uni.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MessageCodec {
    
    // Tamaño fijo de la cabecera: 1 (tipo) + 4 (id) + 4 (longitud) = 9 bytes
    public static final int HEADER_SIZE = 9;

    /**
     * Convierte un objeto ProtocolMessage a un arreglo de bytes listo para el Socket.
     */
    public static byte[] encode(ProtocolMessage message) {
        // Asignar memoria exacta: Cabecera + Carga útil
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + message.getPayloadLength());
        
        // Forzar Big-Endian (Network Byte Order)
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        // Escribir la cabecera
        buffer.put(message.getNodeType());
        buffer.putInt(message.getClientId());
        buffer.putInt(message.getPayloadLength());
        
        // Escribir la carga útil
        buffer.put(message.getPayload());
        
        return buffer.array();
    }

    /**
     * Extrae solo la cabecera de un flujo de bytes. 
     * Útil para saber cuántos bytes leer después en el socket.
     */
    public static ProtocolMessage decodeHeader(byte[] headerBytes) {
        if (headerBytes.length < HEADER_SIZE) {
            throw new IllegalArgumentException("Bytes insuficientes para la cabecera");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(headerBytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        byte nodeType = buffer.get();
        int clientId = buffer.getInt();
        int payloadLength = buffer.getInt();
        
        // Retornamos un mensaje parcial, aún sin el payload
        return new ProtocolMessage(nodeType, clientId, payloadLength, null);
    }
}