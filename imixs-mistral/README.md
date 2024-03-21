# mistral.ai

This is a draft project of Imixs-ML and is just a place to collect some findings about the Open Source LLM 'Mistral 7B'.  

Find also details how to run a LLM in Docker [here](https://ralph.blog.imixs.com/2024/03/19/how-to-run-llms-in-a-docker-container/).

General Sources:

 - https://heidloff.net/article/running-mistral-locally-cpu/
 - https://docs.mistral.ai
 - https://mistral.ai



## Optimizing 

 https://www.e2enetworks.com/blog/a-step-by-step-guide-to-fine-tuning-the-mistral-7b-llm



## Mistral 7B Promt Engeneering:

https://www.promptingguide.ai/models/mistral-7b


https://github.com/ggerganov/llama.cpp/blob/master/examples/main/README.md



 # Build

    $ docker build . -t imixs-mistral-7b

# Run

    $ docker run -it --rm imixs-mistral-7b

    $ docker run -it --gpus all --rm imixs-mistral-7b