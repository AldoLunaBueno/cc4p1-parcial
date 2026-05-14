# Makefile para el Nodo de Imágenes (C++) - Open AI Cubes

# Compilador y banderas
CXX = g++
# -std=c++17: Usa el estándar moderno de C++
# -Wall -Wextra: Muestra todas las advertencias de seguridad
# -Iinclude: Le dice al compilador dónde buscar los archivos .hpp
# -pthread: VITAL para poder usar std::thread (El paralelismo del Issue 3.3)
CXXFLAGS = -std=c++17 -Wall -Wextra -Iinclude -pthread

# Directorios
SRC_DIR = src
OBJ_DIR = obj
BIN_DIR = bin

# Encuentra todos los archivos .cpp en src/ y sus subdirectorios
SOURCES = $(shell find $(SRC_DIR) -name '*.cpp')
# Genera los nombres de los archivos objeto (.o) correspondientes
OBJECTS = $(patsubst $(SRC_DIR)/%.cpp,$(OBJ_DIR)/%.o,$(SOURCES))

# Nombre del ejecutable final
TARGET = $(BIN_DIR)/image_node

# Regla por defecto
all: directories $(TARGET)

# Crea las carpetas base necesarias si no existen
directories:
	@mkdir -p $(OBJ_DIR)
	@mkdir -p $(BIN_DIR)

# Compila el ejecutable uniendo todos los objetos
$(TARGET): $(OBJECTS)
	$(CXX) $(CXXFLAGS) -o $@ $^
	@echo "========================================"
	@echo "Construcción completada: $@"
	@echo "========================================"

# Regla para compilar cada .cpp a un .o (Creando la carpeta dinámicamente)
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp
	@mkdir -p $(dir $@)
	$(CXX) $(CXXFLAGS) -c $< -o $@

# Regla para limpiar los binarios compilados
clean:
	rm -rf $(OBJ_DIR) $(BIN_DIR)
	@echo "Limpieza completada. Directorios obj/ y bin/ eliminados."

# Regla para compilar y ejecutar de una vez
run: all
	./$(TARGET)