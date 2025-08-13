#!/bin/bash
# filepath: src/build

set -e

antlr4-build mus/mus.g4
antlr4-build crs/crs.g4

echo "Build concluído com sucesso."