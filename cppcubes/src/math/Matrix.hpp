#ifndef MATRIX_HPP
#define MATRIX_HPP

#include <vector>

class Matrix {
private:
    int rows;
    int cols;
    // Arreglo 1D plano para máxima localidad de caché (vital para saturar CPU)
    std::vector<double> data;

public:
    Matrix(int r, int c);

    int getRows() const { return rows; }
    int getCols() const { return cols; }

    // Accesos inline para no penalizar el rendimiento en ciclos anidados
    inline double get(int r, int c) const { return data[r * cols + c]; }
    inline void set(int r, int c, double val) { data[r * cols + c] = val; }

    void randomize();
};

#endif // MATRIX_HPP