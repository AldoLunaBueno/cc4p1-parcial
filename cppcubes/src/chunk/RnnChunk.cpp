#include "RnnChunk.hpp"

RnnChunk::RnnChunk(int in, int hidden, int out)
    : inputNodes(in), hiddenNodes(hidden), outputNodes(out) {
    // Redimensionar estructuras
    weightsIH.resize(hiddenNodes, std::vector<double>(inputNodes));
    weightsHO.resize(outputNodes, std::vector<double>(hiddenNodes));
    biasH.resize(hiddenNodes);
    biasO.resize(outputNodes);
}

bool RnnChunk::loadWeights(const std::string& path) {
    std::ifstream file(path);
    if (!file.is_open()) return false;

    std::string line;
    auto leerMatriz = [&](std::vector<std::vector<double>>& mat) {
        for (size_t i = 0; i < mat.size(); ++i) {
            if (std::getline(file, line) && line != "---") {
                std::stringstream ss(line);
                std::string val;
                for (size_t j = 0; j < mat[i].size(); ++j) {
                    if (std::getline(ss, val, ',')) mat[i][j] = std::stod(val);
                }
            }
        }
        std::getline(file, line); // Saltar el "---"
    };

    auto leerVector = [&](std::vector<double>& vec) {
        if (std::getline(file, line) && line != "---") {
            std::stringstream ss(line);
            std::string val;
            for (size_t i = 0; i < vec.size(); ++i) {
                if (std::getline(ss, val, ',')) vec[i] = std::stod(val);
            }
        }
        std::getline(file, line); // Saltar el "---"
    };

    leerMatriz(weightsIH);
    leerVector(biasH);
    leerMatriz(weightsHO);
    leerVector(biasO);

    std::cout << "[RnnChunk] Pesos cargados en RAM exitosamente." << std::endl;
    return true;
}

std::vector<double> RnnChunk::execute(const std::vector<std::vector<double>>& matrix) {
    // DEBUG: Verificar dimensiones de entrada
    if (matrix.empty() || (int)(matrix.size() * matrix[0].size()) != inputNodes) {
        std::cout << "[ERROR RnnChunk] Tamaño incorrecto: " << matrix.size()
                  << "x" << (matrix.empty() ? 0 : matrix[0].size())
                  << " esperado: " << inputNodes << " total." << std::endl;
    }
    // ... resto del código
    // 1. Aplanar (Flatten) la matriz de entrada
    std::vector<double> inputFlat;
    inputFlat.reserve(inputNodes);

    for (const auto& row : matrix) {
        for (double val : row) {
            if (inputFlat.size() < (size_t)inputNodes) inputFlat.push_back(val);
        }
    }

    // 2. Feedforward: Capa Oculta
    std::vector<double> hidden(hiddenNodes);
    for (int i = 0; i < hiddenNodes; ++i) {
        double sum = 0;
        for (int j = 0; j < inputNodes; ++j) {
            sum += inputFlat[j] * weightsIH[i][j];
        }
        hidden[i] = sigmoid(sum + biasH[i]);
    }

    // 3. Feedforward: Capa de Salida
    std::vector<double> output(outputNodes);
    for (int i = 0; i < outputNodes; ++i) {
        double sum = 0;
        for (int j = 0; j < hiddenNodes; ++j) {
            sum += hidden[j] * weightsHO[i][j];
        }
        output[i] = sigmoid(sum + biasO[i]);
    }

    return output;
}
