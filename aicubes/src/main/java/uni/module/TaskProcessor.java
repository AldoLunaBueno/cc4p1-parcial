package uni.module;

public interface TaskProcessor {
    /**
     * Procesa la carga útil (payload) y devuelve el resultado.
     */
    byte[] process(byte[] payload);
}