#!/bin/bash
# filepath: src/compile

set -e

if [ $# -ne 1 ]; then
    echo "Uso: $0 <nome_ficheiro.mus|nome_ficheiro.crs>"
    exit 1
fi

FILENAME="$1"
EXT="${FILENAME##*.}"
EXAMPLES_DIR="../../examples"

if [ "$EXT" = "mus" ]; then
    cd mus
    antlr4-run "$EXAMPLES_DIR/$FILENAME"
    cd ..
elif [ "$EXT" = "crs" ]; then
    cd crs
    antlr4-run "$EXAMPLES_DIR/$FILENAME"
    cat scene-lab.xml
    echo -e "\n"
    cat scene-grd.xml
    echo -e "\n"
    cd ..
else
    echo "Extensão de ficheiro não suportada: $EXT"
    exit 3
fi