#include "ImageNodeServer.hpp"
#include "../chunk/ConvolutionChunk.hpp"
#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <cstring>
#include <stdexcept>
#include <chrono>
#include <vector>

const int HEADER_SIZE = 9;

// --- CONSTRUCTOR ---
ImageNodeServer::ImageNodeServer(int port, int numThreads)
    : port(port), numThreads(numThreads), serverSocket(-1), rnnTool(784, 128, 10) {

    if (!rnnTool.loadWeights("pesos_chatbot.txt")) {
        throw std::runtime_error("Error crítico: No se pudo cargar pesos_chatbot.txt");
    }
}

// --- INICIAR SERVIDOR ---
void ImageNodeServer::start() {
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == -1) throw std::runtime_error("Error al crear socket.");

    int opt = 1;
    setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    sockaddr_in serverAddress{};
    serverAddress.sin_family = AF_INET;
    serverAddress.sin_addr.s_addr = INADDR_ANY;
    serverAddress.sin_port = htons(port);

    if (bind(serverSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) < 0) {
        throw std::runtime_error("Error en el bind.");
    }

    if (listen(serverSocket, 10) < 0) throw std::runtime_error("Error en listen.");

    std::cout << "[C++ Node] Servidor UNI activo en puerto " << port << std::endl;

    while (true) {
        sockaddr_in clientAddress{};
        socklen_t clientLen = sizeof(clientAddress);
        int clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddress, &clientLen);

        if (clientSocket < 0) {
            std::cerr << "Error al aceptar conexión." << std::endl;
            continue;
        }

        handleConnection(clientSocket);
        close(clientSocket);
    }
}

// --- MANEJAR CONEXIÓN ---
void ImageNodeServer::handleConnection(int clientSocket) {
    // 1. Leer Cabecera (9 bytes)
    std::vector<uint8_t> headerBuffer(HEADER_SIZE);
    if (recv(clientSocket, headerBuffer.data(), HEADER_SIZE, MSG_WAITALL) < HEADER_SIZE) return;

    uint8_t nodeType = headerBuffer[0];
    uint32_t clientIdNet, payloadLenNet;
    std::memcpy(&clientIdNet, &headerBuffer[1], 4);
    std::memcpy(&payloadLenNet, &headerBuffer[5], 4);

    uint32_t clientId = ntohl(clientIdNet);
    uint32_t payloadLength = ntohl(payloadLenNet);

    // 2. Leer Payload (Imagen)
    std::vector<uint8_t> payloadBuffer(payloadLength);
    uint32_t totalRead = 0;
    while (totalRead < payloadLength) {
        ssize_t r = recv(clientSocket, reinterpret_cast<char*>(payloadBuffer.data()) + totalRead, payloadLength - totalRead, 0);
        if (r <= 0) break;
        totalRead += static_cast<uint32_t>(r);
    }

    auto start_time = std::chrono::high_resolution_clock::now();

    // 3. Reconstrucción y Normalización
    Matrix image(28, 28);
    double sum = 0.0;
    // En ImageNodeServer::handleConnection
    for (int i = 0; i < 28; ++i) {
        for (int j = 0; j < 28; ++j) {
            // Usar unsigned char explícitamente antes del cast a double
            uint8_t pixelRaw = static_cast<uint8_t>(payloadBuffer[i * 28 + j]);
            double pixelValue = static_cast<double>(pixelRaw) / 255.0;
            image.set(i, j, pixelValue);
        }
    }

    std::cout << "[DEBUG] ID: " << clientId << " | Suma Real: " << sum << std::endl;

    // 4. Convolución (Identidad)
    Matrix kernel(3, 3);
    for (int i = 0; i < 3; ++i) {
        for (int j = 0; j < 3; ++j) {
            kernel.set(i, j, 0.0);
        }
    }
    kernel.set(1, 1, 1.0);

    ConvolutionChunk convChunk;
    Matrix convolved = convChunk.process(image, kernel, numThreads);

    // 5. Inferencia RNN
    std::vector<double> prediction = rnnTool.execute(convolved.toStdVector());

    auto end_time = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);

    // 6. Post-procesamiento
    int digit = 0;
    double maxProb = -1.0;
    for (int i = 0; i < (int)prediction.size(); ++i) {
        if (prediction[i] > maxProb) {
            maxProb = prediction[i];
            digit = i;
        }
    }

    // 7. Enviar Respuesta
    std::string response = "ID:" + std::to_string(clientId) + " | Pred: " + std::to_string(digit) +
                           " | Latencia: " + std::to_string(duration.count()) + "ms";

    sendResponse(clientSocket, nodeType, clientId, response);
}

// --- ENVIAR RESPUESTA ---
void ImageNodeServer::sendResponse(int clientSocket, uint8_t nodeType, uint32_t clientId, const std::string& responseText) {
    uint32_t payloadLen = responseText.length();
    uint32_t clientIdNet = htonl(clientId);
    uint32_t payloadLenNet = htonl(payloadLen);

    std::vector<uint8_t> packet;
    packet.reserve(HEADER_SIZE + payloadLen);

    packet.push_back(nodeType);
    uint8_t* idPtr = reinterpret_cast<uint8_t*>(&clientIdNet);
    packet.insert(packet.end(), idPtr, idPtr + 4);
    uint8_t* lenPtr = reinterpret_cast<uint8_t*>(&payloadLenNet);
    packet.insert(packet.end(), lenPtr, lenPtr + 4);
    packet.insert(packet.end(), responseText.begin(), responseText.end());

    send(clientSocket, packet.data(), packet.size(), 0);
}
