#include <iostream>
#include <string>
#include "server/ImageNodeServer.hpp"

int main(int argc, char* argv[]) {
    // Configuración por defecto: Paralelo
    int numThreads = 4;

    // Si se pasa un argumento por consola (ej: ./image_node 1)
    if (argc > 1) {
        numThreads = std::stoi(argv[1]);
    }

    std::cout << "=====================================================" << std::endl;
    std::cout << "Iniciando infraestructura Open AI Cubes (Modulo C++)" << std::endl;
    std::cout << "Configuración nativa -> Hilos de CPU: " << numThreads << std::endl;
    std::cout << "=====================================================\n" << std::endl;
    
    try {
        ImageNodeServer server(9002, numThreads);
        server.start();
    } catch (const std::exception& e) {
        std::cerr << "Fallo crítico en el servidor nativo: " << e.what() << std::endl;
        return 1;
    }

    return 0;
}