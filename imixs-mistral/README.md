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


### Chat Template for Mistral-7B-Instruct

It's important to note that to effectively prompt the Mistral 7B Instruct and get optimal outputs, it's recommended to use the following chat template:

    <s>[INST] Instruction [/INST] Model answer</s>[INST] Follow-up instruction [/INST]

Note that <s> and </s> are special tokens for beginning of string (BOS) and end of string (EOS) while [INST] and [/INST] are regular strings.


 # Build

    $ docker build . -t imixs-mistral-7b

# Run

    $ docker run -it --rm imixs-mistral-7b

    $ docker run -it --gpus all --rm imixs-mistral-7b


    $ docker run --rm --name="imixs-mistral-7b" -it \
	  -e PYTHONUNBUFFERED=1 \
	  -p 8000:8000 \
	  imixs-mistral-7b

# OpenAPI

We are using [FastAPI](https://fastapi.tiangolo.com/) to provide a developer friendly Open-API Rest Interface. 

    http://127.0.0.1:8000/docs


# Falcon


https://falconframework.org/
https://github.com/falconry/falcon/issues/1718