package uni.chunk;

import uni.math.Matrix;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class MLPChunk {
    private final ExecutorService executor;
    private final int numThreads = 4; // Ajustado para los 4 hilos solicitados 

    public MLPChunk(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Multiplica dos matrices en paralelo saturando los núcleos físicos disponibles.
     */
    public Matrix multiplyParallel(Matrix input, Matrix weights) throws InterruptedException {
        System.out.println("[MLPChunk] Iniciando multiplicación paralela con " + numThreads + " hilos...");
        Matrix result = new Matrix(input.getRows(), weights.getCols());
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        int rowsPerThread = input.getRows() / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int threadIndex = t;
            final int startRow = threadIndex * rowsPerThread;
            final int endRow = (threadIndex == numThreads - 1) ? input.getRows() : startRow + rowsPerThread;

            executor.submit(() -> {
                try {
                    multiplyBlock(input, weights, result, startRow, endRow);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Barrera de sincronización
        System.out.println("[MLPChunk] Multiplicación completada.");
        return result;
    }

    private void multiplyBlock(Matrix A, Matrix B, Matrix C, int startRow, int endRow) {
        double[][] dataA = A.getData();
        double[][] dataB = B.getData();
        double[][] dataC = C.getData();
        int colsB = B.getCols();
        int colsA = A.getCols();

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += dataA[i][k] * dataB[k][j];
                }
                dataC[i][j] = sum;
            }
        }
    }
}