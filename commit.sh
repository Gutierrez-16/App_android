#!/bin/bash

# Colores para la terminal
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Función para traducir texto usando la API de Google Translate
translate_text() {
    local text="$1"
    local source_lang="$2"
    local target_lang="$3"

    local response=$(curl -s -G \
        --data-urlencode "q=${text}" \
        --data-urlencode "sl=${source_lang}" \
        --data-urlencode "tl=${target_lang}" \
        --data-urlencode "dt=t" \
        "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t")

    if [[ $response == *"["* ]]; then
        local translated_text=$(echo "$response" | awk -F'"' '{print $2}')
        echo "$translated_text"
    else
        echo ""
    fi
}

# Función para mostrar las opciones de commit en dos columnas con colores
show_commit_types() {
    echo -e "${CYAN}${BOLD}Seleccione el tipo de commit:${NC}"
    echo -e "${GREEN}${BOLD}1) feat:${NC} Nueva característica\t\t${RED}${BOLD}2) fix:${NC} Bug fix"
    echo -e "${BLUE}${BOLD}3) perf:${NC} Mejoras rendimiento\t\t${YELLOW}${BOLD}4) build:${NC} Cambios en build"
    echo -e "${PURPLE}${BOLD}5) ci:${NC} Integración continua\t\t${CYAN}${BOLD}6) docs:${NC} Documentación"
    echo -e "${GREEN}${BOLD}7) refactor:${NC} Refactorización\t\t${RED}${BOLD}8) style:${NC} Formato"
    echo -e "${BLUE}${BOLD}9) test:${NC} Tests\t\t\t\t${YELLOW}${BOLD}10) add:${NC} Nueva búsqueda"
}

# Función para mostrar las opciones de idioma
show_language_options() {
    echo -e "${CYAN}${BOLD}Seleccione el idioma de traducción:${NC}"
    echo -e "${GREEN}${BOLD}1) en:${NC} Inglés\t\t\t${RED}${BOLD}2) de:${NC} Alemán"
    echo -e "${BLUE}${BOLD}3) zh-CN:${NC} Chino Mandarín"
}

# Solicita al usuario seleccionar el tipo de commit
show_commit_types
read -p "Ingrese el número correspondiente al tipo de commit: " commit_type

# Asocia la selección del usuario con el tipo de commit
case $commit_type in
    1) type="feat";;
    2) type="fix";;
    3) type="perf";;
    4) type="build";;
    5) type="ci";;
    6) type="docs";;
    7) type="refactor";;
    8) type="style";;
    9) type="test";;
    10) type="add";;
    *) echo -e "${RED}Selección inválida${NC}"; exit 1;;
esac

# Solicita al usuario seleccionar el idioma de traducción
show_language_options
read -p "Ingrese el número correspondiente al idioma de traducción: " language_option

# Asocia la selección del usuario con el idioma de traducción
case $language_option in
    1) target_lang="en";;
    2) target_lang="de";;
    3) target_lang="zh-CN";;
    *) echo -e "${RED}Selección inválida${NC}"; exit 1;;
esac

# Solicita el mensaje de commit en español
while true; do
    read -rp "Ingrese el mensaje de commit en español: " message_es
    if [ -n "$message_es" ]; then
        break
    else
        echo -e "${RED}El mensaje de commit no puede estar vacío. Inténtalo de nuevo.${NC}"
    fi
done

# Traduce el mensaje al idioma seleccionado
message_translated=$(translate_text "$message_es" "es" "$target_lang")

# Verifica si la traducción fue exitosa
if [ -z "$message_translated" ]; then
    echo -e "${RED}Error en la traducción. Commit cancelado.${NC}"
    exit 1
fi

# Confirma el mensaje traducido con el usuario
echo -e "${BLUE}${BOLD}Mensaje traducido: ${NC}$message_translated"
read -p "¿Desea usar este mensaje? (s/n): " confirm
if [[ $confirm != "s" ]]; then
    echo -e "${RED}Commit cancelado.${NC}"
    exit 1
fi

# Añade todos los cambios
git add .

# Crea el commit
git commit -m "$type: $message_translated"

# Solicita la rama a la que se quiere hacer push
read -p "Ingrese la rama a la que desea enviar los cambios: " branch

# Realiza el push a la rama especificada
git push origin "$branch"

echo -e "${GREEN}${BOLD}¡Push realizado exitosamente a la rama ${NC}$branch${GREEN}${BOLD}!${NC}"
