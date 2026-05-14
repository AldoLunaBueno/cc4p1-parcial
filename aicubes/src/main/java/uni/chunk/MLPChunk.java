package uni.chunk;

import uni.math.Matrix;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class MLPChunk {
    private final ExecutorService executor;

    public MLPChunk(ExecutorService executor) {
        this.executor = executor; // Puede ser un FixedThreadPool o null para modo secuencial
    }

    public Matrix multiply(Matrix input, Matrix weights, int numThreads) throws InterruptedException {
        // Caso 1 y Caso 3: Secuencial puro (Sin sobrecarga de CountDownLatch)
        if (numThreads <= 1 || executor == null) {
            Matrix result = new Matrix(input.getRows(), weights.getCols());
            multiplyBlock(input, weights, result, 0, input.getRows());
            return result;
        }

        // Caso 2 y Caso 4: Paralelo puro
        Matrix result = new Matrix(input.getRows(), weights.getCols());
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        int rowsPerThread = input.getRows() / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = (t == numThreads - 1) ? input.getRows() : startRow + rowsPerThread;

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