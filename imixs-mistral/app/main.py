from typing import Union
from fastapi import FastAPI

from llama_cpp import Llama
import datamodel

app = FastAPI()


@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}



# Simple Test
# Du bist ein freundlicher und kundiger Reiseführer.
# Wie heist die berühmteste Kirche in München?

@app.post("/chat")
def test_chat(system_message: str,user_message: str):

    # Model parameters
    max_tokens = 2000
    model_path = "mistral-7b-instruct-v0.2.Q4_K_M.gguf"

    # Create a llama model
    model = Llama(
        model_path=model_path,
        temperature=0.75,
        max_tokens=2000,
        top_p=1,    
    )

    prompt = f"""<s>[INST] <<SYS>>{system_message}<</SYS>>{user_message} [/INST]"""
    print("start processing prompt:\n\n",prompt,'\n...\n')
    output = model(prompt, max_tokens=max_tokens, echo=True)

    return output




@app.post("/prompt")
def test_prompt(promptdata: datamodel.PromptEntity):

    # Model parameters
    max_tokens = 2000
    model_path = "mistral-7b-instruct-v0.2.Q4_K_M.gguf"

    # Create a llama model
    model = Llama(
        model_path=model_path,
        temperature=0.75,
        max_tokens=2000,
        top_p=1,    
    )

    prompt = f"""<s>[INST] <<SYS>>{promptdata.system_message}<</SYS>>{promptdata.user_message} [/INST]"""
    print("start processing prompt:\n\n",prompt,'\n...\n')
    output = model(prompt, max_tokens=max_tokens, echo=True)

    return output




