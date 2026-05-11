package uni.node;

public interface TaskProcessor {
    /**
     * Procesa la carga útil (payload) y devuelve el resultado.
     */
    byte[] process(byte[] payload);
}