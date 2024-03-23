#!/bin/bash

############################################################
# The Imixs Developer Interface
# start, build, hot, setup, deploy
# 
############################################################

# Funktion zum Entfernen des '-' Zeichens von einem Parameter
strip_dash() {
    echo "$1" | sed 's/^-//'
}

    echo "     _            _   _          _  "     
    echo "  __| | _____   _(_) | |__   ___| |_ __"  
    echo " / _\` |/ _ \\ \\ / / | | '_ \\ / _ \\ | \'_ \\" 
    echo "| (_| |  __/\ V /| | | | | |  __/ | |_) |"
    echo " \__,_|\___| \_/ |_| |_| |_|\___|_| .__/ "
    echo "                                  |_|  "
    echo "    Imixs Developer Interface..."
    echo "_________________________________________"


if [[ "$(strip_dash $1)" == "start" ]]; then
    echo " Start Dev Environment..."
    docker-compose -f docker-compose.yml up
fi

if [[ "$(strip_dash $1)" == "build" ]]; then
    echo " Build..."
    docker build . -t imixs-mistral-7b
fi

if [[ "$(strip_dash $1)" == "dev" ]]; then
    echo " Start Dev Environment..."
    docker-compose -f docker-compose-dev.yml up
fi




# Überprüfen, ob keine Parameter übergeben wurden - standard build
if [[ $# -eq 0 ]]; then

    echo " Run with ./dev.sh -XXX"
    echo " "
    echo "   -build : build the Docker Image 'imixs-mistral-7b'"
    echo "   -start : starts the Docker Container "
    echo "   -dev   : starts the Docker Container is Dev Mode"
    echo "_________________________________________"
    echo " "

fi