#include "Matrix.hpp"
#include <random>

Matrix::Matrix(int r, int c) : rows(r), cols(c), data(r * c, 0.0) {}

void Matrix::randomize() {
    std::random_device rd;
    std::mt19937 gen(rd()); // Generador Mersenne Twister nativo de C++
    std::uniform_real_distribution<> dis(-1.0, 1.0);

    for (double& val : data) {
        val = dis(gen);
    }
}