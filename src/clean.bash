#!/bin/bash
# filepath: src/build

set -e

antlr4-clean mus/mus.g4
antlr4-clean crs/crs.g4

if [ -f mus/Output.java ]; then
    rm mus/Output.java
fi

if [ -f crs/scene-lab.xml ]; then
    rm crs/scene-lab.xml
fi

if [ -f crs/scene-grd.xml ]; then
    rm crs/scene-grd.xml
fi

echo "Clean concluído com sucesso."