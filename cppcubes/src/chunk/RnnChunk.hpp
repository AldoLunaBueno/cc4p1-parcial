#ifndef RNN_CHUNK_HPP
#define RNN_CHUNK_HPP

#include <vector>
#include <string>
#include <cmath>
#include <fstream>
#include <sstream>
#include <iostream>

class RnnChunk {
private:
    int inputNodes, hiddenNodes, outputNodes;
    // Usamos vectores de vectores para los pesos y vectores simples para los bias
    std::vector<std::vector<double>> weightsIH;
    std::vector<std::vector<double>> weightsHO;
    std::vector<double> biasH;
    std::vector<double> biasO;

    double sigmoid(double x) {
        return 1.0 / (1.0 + std::exp(-x));
    }

public:
    RnnChunk(int in, int hidden, int out);

    // Este método se llama UNA SOLA VEZ al inicio para subir a RAM
    bool loadWeights(const std::string& path);

    // Esta es la función que llama tu módulo repetidamente
    // Recibe la matriz que sale del ConvolutionChunk
    std::vector<double> execute(const std::vector<std::vector<double>>& matrix);
};

#endif
