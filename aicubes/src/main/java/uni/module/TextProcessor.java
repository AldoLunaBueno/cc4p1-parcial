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
    private final int numThreads;

    public TextProcessor(int numThreads) {
        this.numThreads = numThreads;
        this.embeddingChunk = new EmbeddingChunk();
        
        // Si es 1 hilo (Caso 1 y 3), no hay pool. Si es > 1 (Caso 2 y 4), creamos el pool.
        if (numThreads > 1) {
            this.workerPool = Executors.newFixedThreadPool(numThreads);
        } else {
            this.workerPool = null;
        }
        this.mlpChunk = new MLPChunk(workerPool);
    }

    @Override
    public byte[] process(byte[] payload) {
        try {
            String text = new String(payload);
            Matrix textEmbeddings = embeddingChunk.process(text, 1000, 1000);
            
            Matrix mlpWeights = new Matrix(1000, 1000);
            mlpWeights.randomize();

            // === INICIO DE MEDICIÓN DE TIEMPO ===
            long startTime = System.currentTimeMillis();
            
            // Usamos el método multiply que adaptamos para aceptar numThreads
            Matrix finalOutput = mlpChunk.multiply(textEmbeddings, mlpWeights, numThreads);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            // === FIN DE MEDICIÓN ===

            String responseStr = "Inferencia (" + numThreads + " hilos) completada en " + duration + "ms. " +
                                 "Dimensión: " + finalOutput.getRows() + "x" + finalOutput.getCols();
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