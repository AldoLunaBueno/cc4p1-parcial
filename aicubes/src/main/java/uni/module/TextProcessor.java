package uni.module;

import uni.chunk.EmbeddingChunk;
import uni.chunk.MLPChunk;
import uni.math.Matrix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextProcessor implements TaskProcessor {
    private final EmbeddingChunk embeddingChunk;
    private final MLPChunk mlpChunk;
    private final ExecutorService workerPool;

    public TextProcessor() {
        this.embeddingChunk = new EmbeddingChunk();
        // Pool de 4 hilos fijos dedicado a los micro chunks matemáticos
        this.workerPool = Executors.newFixedThreadPool(4); 
        this.mlpChunk = new MLPChunk(workerPool);
    }

    @Override
    public byte[] process(byte[] payload) {
        try {
            // 1. Recepción y decodificación
            String text = new String(payload);
            
            // 2. Llamada al Micro Chunk de Embeddings
            // Generamos una matriz de 1000x1000 para saturar la CPU
            Matrix textEmbeddings = embeddingChunk.process(text, 1000, 1000);
            
            // Simulación de los pesos de la red neuronal (Weights)
            Matrix mlpWeights = new Matrix(1000, 1000);
            mlpWeights.randomize();

            // 3. Llamada al Micro Chunk de MLP (Procesamiento Paralelo)
            Matrix finalOutput = mlpChunk.multiplyParallel(textEmbeddings, mlpWeights);

            // 4. Empaquetado final (Simulamos la respuesta del chat)
            String responseStr = "Inferencia paralela completada. Dimensión de salida: " + 
                                 finalOutput.getRows() + "x" + finalOutput.getCols();
            return responseStr.getBytes();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ERROR_INTERRUPCION_HILOS".getBytes();
        } catch (Exception e) {
            return ("ERROR_INTERNO: " + e.getMessage()).getBytes();
        }
    }
    
    // Método para limpiar recursos al apagar el nodo
    public void shutdown() {
        if (workerPool != null && !workerPool.isShutdown()) {
            workerPool.shutdown();
        }
    }
}