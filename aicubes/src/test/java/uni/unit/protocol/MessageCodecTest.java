package uni.unit.protocol;

import org.junit.jupiter.api.Test;

import uni.protocol.MessageCodec;
import uni.protocol.ProtocolMessage;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

class MessageCodecTest {

    private final byte TEST_NODE_TYPE = 0x01;
    private final int TEST_CLIENT_ID = 812;
    private final String TEST_TEXT = "Hola, Open AI Cubes!";
    private final byte[] TEST_PAYLOAD = TEST_TEXT.getBytes();

    @Test
    void encode_ValidMessage_ReturnsCorrectByteArray() {
        // Arrange
        ProtocolMessage message = new ProtocolMessage(TEST_NODE_TYPE, TEST_CLIENT_ID, TEST_PAYLOAD);
        int expectedTotalLength = MessageCodec.HEADER_SIZE + TEST_PAYLOAD.length;

        // Act
        byte[] encodedBytes = MessageCodec.encode(message);

        // Assert
        assertNotNull(encodedBytes);
        assertEquals(expectedTotalLength, encodedBytes.length, "La longitud total del arreglo de bytes es incorrecta");
        assertEquals(TEST_NODE_TYPE, encodedBytes[0], "El primer byte debe ser el tipo de nodo");
    }

    @Test
    void decodeHeader_ValidHeaderBytes_ReturnsCorrectHeaderObject() {
        // Arrange
        ProtocolMessage originalMessage = new ProtocolMessage(TEST_NODE_TYPE, TEST_CLIENT_ID, TEST_PAYLOAD);
        byte[] encodedBytes = MessageCodec.encode(originalMessage);
        
        // Simulamos que el socket lee solo los primeros 9 bytes
        byte[] headerBytes = Arrays.copyOfRange(encodedBytes, 0, MessageCodec.HEADER_SIZE);

        // Act
        ProtocolMessage decodedHeader = MessageCodec.decodeHeader(headerBytes);

        // Assert
        assertNotNull(decodedHeader);
        assertEquals(TEST_NODE_TYPE, decodedHeader.getNodeType(), "El tipo de nodo no coincide");
        assertEquals(TEST_CLIENT_ID, decodedHeader.getClientId(), "El ID del cliente no coincide");
        assertEquals(TEST_PAYLOAD.length, decodedHeader.getPayloadLength(), "La longitud del payload no coincide");
        assertNull(decodedHeader.getPayload(), "El payload debe ser nulo al decodificar solo la cabecera");
    }

    @Test
    void decodeHeader_InsufficientBytes_ThrowsIllegalArgumentException() {
        // Arrange
        // Simulamos un socket que leyó incompleto (solo 5 bytes en lugar de 9)
        byte[] incompleteHeaderBytes = new byte[]{0x01, 0x00, 0x00, 0x03, 0x2C};

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            MessageCodec.decodeHeader(incompleteHeaderBytes);
        });

        assertTrue(exception.getMessage().contains("Bytes insuficientes"));
    }
}