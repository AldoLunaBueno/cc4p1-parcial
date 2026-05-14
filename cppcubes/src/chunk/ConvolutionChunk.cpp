#include "ConvolutionChunk.hpp"
#include <thread>

Matrix ConvolutionChunk::process(const Matrix& input, const Matrix& kernel, int numThreads) {
    int inRows = input.getRows();
    int inCols = input.getCols();
    int kRows = kernel.getRows();
    int kCols = kernel.getCols();

    // Calculamos el padding necesario para que la salida sea del mismo tamaño
    int padY = kRows / 2;
    int padX = kCols / 2;

    Matrix output(inRows, inCols);

    // Iteramos sobre cada "píxel" de la matriz de entrada
    auto computeBlock = [&](int startRow, int endRow) {
        for (int i = startRow; i < endRow; ++i) {
            for (int j = 0; j < inCols; ++j) {
                double sum = 0.0;
                
                // Aplicamos el Kernel (matriz pequeña, usualmente 3x3 o 5x5)
                for (int ki = 0; ki < kRows; ++ki) {
                    for (int kj = 0; kj < kCols; ++kj) {
                        // Mapeamos a las coordenadas de la imagen original
                        int r = i + ki - padY;
                        int c = j + kj - padX;

                        // Zero padding: Solo multiplicamos si estamos dentro de los límites
                        if (r >= 0 && r < inRows && c >= 0 && c < inCols) {
                            sum += input.get(r, c) * kernel.get(ki, kj);
                        }
                    }
                }
                output.set(i, j, sum);
            }
        }
    };

    // Caso 1 y Caso 3: Ejecución Puramente Secuencial
    if (numThreads <= 1) {
        computeBlock(0, inRows);
        return output;
    }

    // Caso 2 y Caso 4: Ejecución Paralela
    std::vector<std::thread> workers;
    int rowsPerThread = inRows / numThreads;

    for (int t = 0; t < numThreads; ++t) {
        int startRow = t * rowsPerThread;
        int endRow = (t == numThreads - 1) ? inRows : startRow + rowsPerThread;
        workers.emplace_back(computeBlock, startRow, endRow);
    }

    for (auto& t : workers) {
        if (t.joinable()) t.join();
    }
    
    return output;
}