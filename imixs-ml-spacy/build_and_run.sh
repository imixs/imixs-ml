#!/bin/bash

############################################################
# Build Docker image and run container local
# 
############################################################

docker build --tag=imixs/imixs-ml-spacy .
docker run --rm --name="imixs-ml-spacy" -it   -p 8000:8000   imixs/imixs-ml-spacy