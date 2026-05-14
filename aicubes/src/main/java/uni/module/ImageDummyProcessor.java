package uni.module;

public class ImageDummyProcessor implements TaskProcessor {
    @Override
    public byte[] process(byte[] payload) {
        System.out.println("[Nodo Imagen] Aplicando convoluciones...");
        try {
            // Emular carga de trabajo de 2 segundos
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "Imagen procesada exitosamente (Dummy)".getBytes();
    }
}