# CC4P1 - Examen Parcial

Examen Parcial del curso Programación Concurrente y Distribuida.
Proyecto: Open AI Cubes.

## Plan de desarrollo

### Sprint 1: El Esqueleto de Red (Secuencial Homogéneo)

**Objetivo:** Lograr el flujo de comunicación de extremo a extremo (Frontend $\rightarrow$ Servidor Central $\rightarrow$ Nodo) en un entorno puramente secuencial (1 hilo) y en un solo lenguaje (Java). No hay matemáticas complejas aún, solo transferencia de bytes.

* **Issue 1.1: Definición del Protocolo Binario Estándar**
* Diseñar la estructura de la cabecera de los mensajes TCP (ej. `[Tipo_Nodo(1 byte)] [ID_Cliente(4 bytes)] [Longitud_Payload(4 bytes)] [Cuerpo]`).

* **Issue 1.2: Servidor Central - Enrutador Base (Java)**
* Implementar un `ServerSocket` que escuche conexiones.
* Crear una capa de dominio (separada de la capa de red) que reciba el paquete, extraiga el identificador numérico del cliente y determine a qué nodo despacharlo, manteniendo la pureza de la lógica de negocio sin mezclarla con el manejo crudo de los *sockets*.

* **Issue 1.3: Nodos "Dummy" Secuenciales (Java)**
* Crear dos ejecutables separados (Texto e Imagen) que escuchen en puertos distintos.
* Al recibir un paquete, estos nodos simplemente aplicarán un retardo (`Thread.sleep`) y devolverán un mensaje predeterminado ("Texto procesado", "Imagen procesada").

* **Issue 1.4: Cliente UI y Conexión (Java)**
* Construir la interfaz de escritorio básica.
* Conectar el cliente al Servidor Central enviando comandos simples.

* **Hito del Sprint:** Al ejecutar el cliente, el Servidor Central enruta la petición al nodo correcto y devuelve la respuesta a la interfaz.

### Sprint 2: El Motor de Concurrencia (Paralelismo Homogéneo)

**Objetivo:** Reemplazar el procesamiento "dummy" con los *Micro chunks* matemáticos reales y habilitar la concurrencia en el Servidor Central.

* **Issue 2.1: Servidor Central Multihilo (Java)**
* Implementar un `ThreadPoolExecutor` (o manejo manual de hilos) en el Servidor Central para que no se bloquee al recibir múltiples clientes simultáneos.

* **Issue 2.2: Matemáticas de los Micro Chunks (Java)**
* Implementar desde cero la estructura de datos para matrices.
* Programar el algoritmo manual de multiplicación de matrices para el *Micro chunk* del MLP (Multilayer Perceptron).
* Programar un algoritmo de búsqueda/mapeo básico para el *Micro chunk* de Embeddings.

* **Issue 2.3: Paralelización en el Nodo de Texto (Java)**
* Modificar el Nodo de Texto para que, al recibir un *payload*, divida las matrices en 4 bloques.
* Lanzar 4 hilos locales, cada uno resolviendo un bloque del MLP utilizando la CPU al máximo, y unificar el resultado.

* **Hito del Sprint:** El sistema ejecuta el Caso 1 al 100%. Las peticiones de texto generan carga real en los núcleos de la máquina simulando el cálculo de pesos del MLP.

### Sprint 3: Ecosistema Heterogéneo (Integración C++)

**Objetivo:** Cumplir con el "Caso 2" introduciendo un nuevo lenguaje y abordando la interoperabilidad binaria.

* **Issue 3.1: Listener TCP en C++ (El Nodo de Imágenes)**
* Implementar sockets de bajo nivel (POSIX) en C++ para escuchar las peticiones del Servidor Central.
* Asegurar la correcta serialización/deserialización del protocolo binario, controlando el *Endianness* (orden de los bytes) entre la máquina virtual de Java y el binario nativo de C++.

* **Issue 3.2: Micro Chunk de Convoluciones (C++ STL)**
* Aprovechar el rendimiento de la Standard Template Library (STL) para gestionar matrices de vectores (`std::vector`).
* Implementar el algoritmo matemático de una convolución (aplicar un kernel sobre una matriz 2D simulando una imagen).

* **Issue 3.3: Paralelismo en C++**
* Dividir el trabajo de la convolución utilizando `std::thread`, procesando distintos cuadrantes de la matriz de forma paralela.

* **Hito del Sprint:** El Servidor Central de Java ahora se comunica fluidamente con el Nodo de Imágenes en C++, delegando el trabajo pesado a la memoria nativa y procesando los *micro chunks* en paralelo.

### Sprint 4: Pruebas de Estrés y Despliegue (Distribución Total)

**Objetivo:** Saturar el sistema, llevarlo a un entorno de red real y recolectar la telemetría para el informe final.

* **Issue 4.1: Cliente de Pruebas de Carga**
* Crear un *script* automatizado que instancie cientos de sockets clientes, disparando transacciones con un *delay* aleatorio hacia el Servidor Central.

* **Issue 4.2: Despliegue Distribuido**
* Desplegar los módulos en redes separadas. Inicialmente, probar levantando los nodos en diferentes terminales aisladas dentro de su entorno local (como instancias de WSL2) para asegurar el ruteo interno.
* Opcional: Desplegar en infraestructura en la nube para probar la latencia real.

* **Issue 4.3: Instrumentación y Telemetría**
* Agregar contadores de tiempo en los nodos (`System.nanoTime()` en Java o `std::chrono` en C++) para separar el "tiempo de cálculo matemático" del "tiempo de latencia de red".
* Generar los logs que servirán para construir las gráficas del informe en PDF (comparando secuencial vs. paralelo vs. distribuido).

* **Hito del Sprint:** El sistema sobrevive al ataque de múltiples clientes, la CPU de los nodos se satura eficientemente sin caídas, y se obtienen los datos empíricos para la exposición.

---

Para arrancar con el Sprint 1, ¿quieren que esbocemos la estructura de bytes exacta que tendrá la cabecera de su protocolo para que sirva tanto para Java como para el futuro nodo en C++?
