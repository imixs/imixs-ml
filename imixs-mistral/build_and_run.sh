#!/bin/bash

############################################################
# Build Docker image and run container local
# 
############################################################

docker build . -t imixs-mistral-7b
docker run --rm --name="imixs-mistral-7b" -it \
	  -e PYTHONUNBUFFERED=1 \
	  -p 8000:8000 \
	  imixs-mistral-7b