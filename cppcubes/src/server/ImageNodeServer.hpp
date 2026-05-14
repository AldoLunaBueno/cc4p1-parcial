#ifndef IMAGE_NODE_SERVER_HPP
#define IMAGE_NODE_SERVER_HPP

#include <string>
#include <vector>
#include <cstdint>

class ImageNodeServer {
public:
    ImageNodeServer(int port, int numThreads);
    void start();

private:
    int port;
    int numThreads;
    int serverSocket;

    void handleConnection(int clientSocket);
    void sendResponse(int clientSocket, uint8_t nodeType, uint32_t clientId, const std::string& responseText);

    // --- MANEJO DE SEÑALES PARA CIERRE ELEGANTE ---
    static int activeServerSocket;
    static void signalHandler(int signum);
};

#endif // IMAGE_NODE_SERVER_HPP