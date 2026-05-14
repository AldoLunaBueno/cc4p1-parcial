#ifndef CONVOLUTION_CHUNK_HPP
#define CONVOLUTION_CHUNK_HPP

#include "../math/Matrix.hpp"

class ConvolutionChunk {
public:
    Matrix process(const Matrix& input, const Matrix& kernel, int numThreads);
};

#endif // CONVOLUTION_CHUNK_HPP