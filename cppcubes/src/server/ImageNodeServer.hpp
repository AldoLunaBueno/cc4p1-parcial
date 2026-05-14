#ifndef IMAGE_NODE_SERVER_HPP
#define IMAGE_NODE_SERVER_HPP

#include <string>
#include <vector>
#include <cstdint>
#include <netinet/in.h>
#include "../chunk/RnnChunk.hpp"

class ImageNodeServer {
public:
    // Constructor con puerto y configuración de hilos para procesamiento paralelo
    ImageNodeServer(int port, int numThreads);
    void start();

private:
    int port;
    int numThreads;
    int serverSocket;
    RnnChunk rnnTool; // La red neuronal persiste en RAM para evitar recargas de pesos

    void handleConnection(int clientSocket);
    void sendResponse(int clientSocket, uint8_t nodeType, uint32_t clientId, const std::string& responseText);

    // --- MANEJO DE SEÑALES PARA CIERRE ELEGANTE ---
    static int activeServerSocket;
    static void signalHandler(int signum);
};

#endif // IMAGE_NODE_SERVER_HPP
