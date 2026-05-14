package uni.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uni.chunk.MLPChunk;
import uni.math.Matrix;
import uni.module.TextProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyTest {

    private ExecutorService testPool;
    private MLPChunk mlpChunk;

    @BeforeEach
    void setUp() {
        // Configuramos un pool idéntico al de producción (4 hilos)
        testPool = Executors.newFixedThreadPool(4);
        mlpChunk = new MLPChunk(testPool);
    }

    @AfterEach
    void tearDown() {
        if (testPool != null) {
            testPool.shutdown();
        }
    }

    @Test
    void testParallelMultiplicationExactMath() throws InterruptedException {
        // Preparamos dos matrices pequeñas (4x4) para validar la matemática exacta
        Matrix A = new Matrix(4, 4);
        Matrix B = new Matrix(4, 4);

        // Llenamos las matrices con valores conocidos (ej. 2.0)
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                A.getData()[i][j] = 2.0;
                B.getData()[i][j] = 3.0;
            }
        }

        // Ejecutamos la multiplicación concurrente
        Matrix result = mlpChunk.multiply(A, B, 4);

        // Verificamos que el algoritmo paralelo ensambla bien los bloques
        // Si A está llena de 2s y B de 3s, cada celda de C debería ser: 4 * (2 * 3) = 24.0
        assertEquals(4, result.getRows());
        assertEquals(4, result.getCols());
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(24.0, result.getData()[i][j], 0.0001, 
                    "Fallo matemático en la celda [" + i + "][" + j + "]. Revisa las condiciones de carrera.");
            }
        }
    }

@Test
    void testTextProcessorIntegration() {
        // Probamos que el orquestador general divide bien las tareas y espera a los chunks
        int hilosPrueba = 4;
        TextProcessor processor = new TextProcessor(hilosPrueba);
        String dummyPayload = "Hola Open AI Cubes";
        
        byte[] response = processor.process(dummyPayload.getBytes());
        String responseStr = new String(response);
        
        // Verificamos que el string de retorno contenga la nueva firma con el número de hilos
        assertTrue(responseStr.contains("Inferencia (" + hilosPrueba + " hilos) completada"), 
            "El procesador devolvió un error, no terminó, o el texto de respuesta no coincide. Recibido: " + responseStr);
        assertTrue(responseStr.contains("1000x1000"), 
            "Las dimensiones de la matriz final no son las esperadas.");
            
        // Limpiamos los hilos del procesador
        processor.shutdown();
    }
}