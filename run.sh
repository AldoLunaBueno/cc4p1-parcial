#!/bin/bash

# ==========================================
# Open AI Cubes - Orquestador de Pruebas
# ==========================================

echo ">>> [1/4] Compilando el Nodo de Imagen en C++..."
make -C cppcubes clean
make -C cppcubes

# Opcional: Si usas Maven para compilar Java, puedes descomentar la siguiente línea:
# mvn -f aicubes/pom.xml compile

echo ""
echo ">>> [2/4] Levantando el Nodo de Imagen (C++) en segundo plano..."
# Ejecutamos con 4 hilos y enviamos la salida a /dev/null para no ensuciar la consola del test
./cppcubes/bin/image_node 4 > /dev/null 2>&1 &
CPP_PID=$!

echo ">>> [3/4] Levantando el Servidor Central y Nodo de Texto (Java) en segundo plano..."
# Usamos ServerLauncher: 4 hilos, false = No levantar nodo de imagen en Java
java -cp aicubes/target/classes uni.server.ServerLauncher 4 false > /dev/null 2>&1 &
JAVA_PID=$!

# Función para limpiar los puertos al terminar
cleanup() {
    echo ""
    echo ">>> Limpiando procesos en segundo plano..."
    kill $CPP_PID 2>/dev/null
    kill $JAVA_PID 2>/dev/null
    echo ">>> Entorno limpio. ¡Prueba finalizada!"
    exit 0
}

# Atrapamos señales de cierre (Ctrl+C o fin del script) para ejecutar cleanup
trap cleanup EXIT INT TERM

echo ">>> Esperando 3 segundos para que los puertos se abran correctamente..."
sleep 3

echo ""
echo ">>> [4/4] INICIANDO PRUEBA DE ESTRÉS..."
echo "=================================================="
# Ejecutamos el tester
java -cp aicubes/target/classes uni.client.StressTester

# Al terminar el StressTester de forma natural, el script llegará aquí
# y la trampa 'EXIT' invocará a cleanup automáticamente.