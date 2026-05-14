#include "ImageNodeServer.hpp"
#include "../chunk/ConvolutionChunk.hpp"
#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <cstring>
#include <stdexcept>
#include <chrono> // Para medir el tiempo

// Tamaño fijo de tu protocolo: 1 byte (Tipo) + 4 bytes (ID) + 4 bytes (Longitud)
const int HEADER_SIZE = 9;

ImageNodeServer::ImageNodeServer(int port, int numThreads) : port(port), numThreads(numThreads), serverSocket(-1) {}

void ImageNodeServer::start() {
    // 1. Crear el socket (IPv4, TCP)
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == -1) {
        throw std::runtime_error("Error al crear el socket POSIX.");
    }

    // Permitir reusar el puerto inmediatamente después de cerrar el servidor
    int opt = 1;
    setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    // 2. Configurar la dirección
    sockaddr_in serverAddress{};
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_addr.s_addr = INADDR_ANY; // Escuchar en todas las interfaces
    serverAddress.sin_port = htons(port);       // Host To Network Short

    // 3. Bind
    if (bind(serverSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) < 0) {
        throw std::runtime_error("Error en el bind del puerto " + std::to_string(port));
    }

    // 4. Listen
    if (listen(serverSocket, 10) < 0) {
        throw std::runtime_error("Error al escuchar en el puerto.");
    }

    std::cout << "[C++ Node] Servidor de Imágenes (POSIX) iniciado en el puerto: " << port << std::endl;

    // 5. Ciclo de aceptación (Secuencial por ahora, paralelismo viene en Issue 3.3)
    while (true) {
        sockaddr_in clientAddress{};
        socklen_t clientLen = sizeof(clientAddress);
        int clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddress, &clientLen);
        
        if (clientSocket < 0) {
            std::cerr << "Error al aceptar conexión." << std::endl;
            continue;
        }

        handleConnection(clientSocket);
        close(clientSocket); // Cerramos después de responder
    }
}

void ImageNodeServer::handleConnection(int clientSocket) {
    // A. Leer la cabecera (9 bytes)
    std::vector<uint8_t> headerBuffer(HEADER_SIZE);
    int bytesRead = recv(clientSocket, headerBuffer.data(), HEADER_SIZE, MSG_WAITALL);
    
    if (bytesRead < HEADER_SIZE) {
        std::cerr << "Cabecera incompleta recibida." << std::endl;
        return;
    }

    // B. Decodificar controlando el Endianness
    uint8_t nodeType = headerBuffer[0];
    
    uint32_t clientIdNet, payloadLenNet;
    std::memcpy(&clientIdNet, &headerBuffer[1], 4);
    std::memcpy(&payloadLenNet, &headerBuffer[5], 4);
    
    // ntohl: Network TO Host Long
    uint32_t clientId = ntohl(clientIdNet);
    uint32_t payloadLength = ntohl(payloadLenNet);

    std::cout << "[C++] Petición recibida - ID Cliente: " << clientId 
              << ", Longitud Payload: " << payloadLength << " bytes." << std::endl;

    // C. Leer el Payload
    std::vector<char> payloadBuffer(payloadLength);
    int payloadRead = 0;
    while (payloadRead < payloadLength) {
        int r = recv(clientSocket, payloadBuffer.data() + payloadRead, payloadLength - payloadRead, 0);
        if (r <= 0) break;
        payloadRead += r;
    }

    // D. Procesamiento Real de Convolución
    std::cout << "[C++] Iniciando convolución paralela en matriz 2000x2000..." << std::endl;

    // Simulamos una imagen grande (2000x2000)
    Matrix image(2000, 2000);
    image.randomize();

    // Kernel típico de 3x3 (ej. detección de bordes)
    Matrix kernel(3, 3);
    kernel.randomize();

    ConvolutionChunk convChunk;
    // Medición de tiempo con alta resolución en C++
    auto start_time = std::chrono::high_resolution_clock::now();
    
    // Le pasamos numThreads a tu chunk
    Matrix result = convChunk.process(image, kernel, numThreads);
    
    auto end_time = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
    // === FIN PROCESAMIENTO ===

    std::string response = "Convolucion nativa (" + std::to_string(numThreads) + " hilos) completada en " 
                           + std::to_string(duration.count()) + "ms. Dimension: " 
                           + std::to_string(result.getRows()) + "x" + std::to_string(result.getCols());

    // E. Enviar respuesta empaquetada
    sendResponse(clientSocket, nodeType, clientId, response);
}

void ImageNodeServer::sendResponse(int clientSocket, uint8_t nodeType, uint32_t clientId, const std::string& responseText) {
    uint32_t payloadLen = responseText.length();

    // htonl: Host TO Network Long
    uint32_t clientIdNet = htonl(clientId);
    uint32_t payloadLenNet = htonl(payloadLen);

    // Empaquetar
    std::vector<uint8_t> responsePacket;
    responsePacket.reserve(HEADER_SIZE + payloadLen);
    
    responsePacket.push_back(nodeType);
    
    uint8_t* idPtr = reinterpret_cast<uint8_t*>(&clientIdNet);
    responsePacket.insert(responsePacket.end(), idPtr, idPtr + 4);
    
    uint8_t* lenPtr = reinterpret_cast<uint8_t*>(&payloadLenNet);
    responsePacket.insert(responsePacket.end(), lenPtr, lenPtr + 4);
    
    // Agregar payload
    responsePacket.insert(responsePacket.end(), responseText.begin(), responseText.end());

    // Enviar
    send(clientSocket, responsePacket.data(), responsePacket.size(), 0);
}