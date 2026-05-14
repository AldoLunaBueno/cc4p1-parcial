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

    // Agrega esto dentro de tu clase Matrix
    std::vector<std::vector<double>> toStdVector() const {
        std::vector<std::vector<double>> result(rows, std::vector<double>(cols));
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                // Usa tu método get(i, j) o el acceso que tengas a los datos
                result[i][j] = this->get(i, j);
            }
        }
        return result;
    }
};

#endif // MATRIX_HPP
