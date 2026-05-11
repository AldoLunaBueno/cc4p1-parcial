package uni.protocol;

public class ProtocolMessage {
    private final byte nodeType;
    private final int clientId;
    private final int payloadLength;
    private final byte[] payload;

    public ProtocolMessage(byte nodeType, int clientId, byte[] payload) {
        this.nodeType = nodeType;
        this.clientId = clientId;
        this.payloadLength = payload.length;
        this.payload = payload;
    }

    // Constructor interno usado por el Codec al deserializar
    protected ProtocolMessage(byte nodeType, int clientId, int payloadLength, byte[] payload) {
        this.nodeType = nodeType;
        this.clientId = clientId;
        this.payloadLength = payloadLength;
        this.payload = payload;
    }

    public byte getNodeType() { return nodeType; }
    public int getClientId() { return clientId; }
    public int getPayloadLength() { return payloadLength; }
    public byte[] getPayload() { return payload; }
}