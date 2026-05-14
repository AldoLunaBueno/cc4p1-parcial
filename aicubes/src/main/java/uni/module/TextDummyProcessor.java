package uni.module;

public class TextDummyProcessor implements TaskProcessor {
    @Override
    public byte[] process(byte[] payload) {
        System.out.println("[Nodo Texto] Analizando embeddings y calculando MLP...");
        try {
            // Emular carga de trabajo de 1.5 segundos
            Thread.sleep(1500); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String received = new String(payload);
        String response = "Texto procesado exitosamente: [" + received + "]";
        return response.getBytes();
    }
}