#!/bin/bash
# filepath: src/run

set -e

if [ $# -ne 1 ]; then
    echo "Uso: $0 <Output.java>"
    exit 1
fi

INPUT="$1"
EXT="${INPUT##*.}"

if [ "$EXT" = "java" ]; then
    # Copia Output.java para ciber/jAgents (overwrite)
    cp -f mus/Output.java ../ciber/jAgents/

    # Compila na pasta ciber/jAgents
    cd ../ciber/jAgents
    javac Output.java

    # Executa build e run do simulador (ajusta se necessário)
    ./build -a Output.java
    ./run -a Output

    cd ../../src
    echo "Sucesso"
else
    echo "Só é suportado ficheiro .java (Output.java)."
    exit 2
fi