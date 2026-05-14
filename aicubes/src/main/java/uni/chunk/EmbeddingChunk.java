package uni.chunk;

import uni.math.Matrix;

public class EmbeddingChunk {
    
    /**
     * Simula el algoritmo de búsqueda/mapeo convirtiendo texto en una matriz pesada.
     */
    public Matrix process(String text, int rows, int cols) {
        System.out.println("[EmbeddingChunk] Mapeando texto a vectores: " + text);
        // Instanciamos una matriz grande para simular una carga pesada de embeddings
        Matrix embeddings = new Matrix(rows, cols);
        embeddings.randomize(); 
        return embeddings;
    }
}