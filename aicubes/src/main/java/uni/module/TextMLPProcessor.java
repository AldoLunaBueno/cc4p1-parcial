package uni.module;

import uni.math.Matrix;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextMLPProcessor implements TaskProcessor {
    private final ExecutorService executor;
    private final int numThreads = 4;

    public TextMLPProcessor() {
        // Pool de tamaño fijo porque es una tarea puramente CPU-bound
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    @Override
    public byte[] process(byte[] payload) {
        // 1. Decodificar texto a "Embeddings" (Simulado por ahora)
        String text = new String(payload);
        System.out.println("Procesando texto para MLP: " + text.substring(0, Math.min(20, text.length())) + "...");

        // Instanciamos matrices grandes para saturar la CPU (Ej: 1000x1000)
        int size = 1000;
        Matrix input = new Matrix(size, size);
        Matrix weights = new Matrix(size, size);
        input.randomize();
        weights.randomize();

        Matrix result = new Matrix(input.getRows(), weights.getCols());

        // 2. División de bloques
        CountDownLatch latch = new CountDownLatch(numThreads);
        int rowsPerThread = input.getRows() / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int threadIndex = t;
            final int startRow = threadIndex * rowsPerThread;
            // El último hilo asume el resto si la división no es exacta
            final int endRow = (threadIndex == numThreads - 1) ? input.getRows() : startRow + rowsPerThread;

            executor.submit(() -> {
                try {
                    multiplyBlock(input, weights, result, startRow, endRow);
                } finally {
                    latch.countDown(); // Señaliza que este hilo terminó su bloque
                }
            });
        }

        // 3. Barrera de sincronización (Esperar a los 4 hilos)
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ERROR_INTERRUPCION".getBytes();
        }

        // Aquí se aplicaría la función de activación (Ej: ReLU) y el empaquetado final
        return "MLP_INFERENCIA_COMPLETADA".getBytes();
    }

    /**
     * Calcula una subsección de la matriz resultante.
     * Operación pura, lee de A y B, escribe en una sección aislada de C.
     */
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