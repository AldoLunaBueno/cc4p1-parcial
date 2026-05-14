#ifndef IMAGE_NODE_SERVER_HPP
#define IMAGE_NODE_SERVER_HPP

#include <string>
#include <vector>
#include <cstdint>

class ImageNodeServer {
public:
    // Agregamos numThreads al constructor
    ImageNodeServer(int port, int numThreads);
    void start();

private:
    int port;
    int numThreads; // Almacenamos la configuración de hilos
    int serverSocket;

    void handleConnection(int clientSocket);
    void sendResponse(int clientSocket, uint8_t nodeType, uint32_t clientId, const std::string& responseText);
};

#endif // IMAGE_NODE_SERVER_HPP