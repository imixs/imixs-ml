from typing import Union
from dataclasses import dataclass, field
from fastapi import FastAPI
from fastapi_xml import add_openapi_extension
from fastapi_xml import XmlRoute
from fastapi_xml import XmlAppResponse
from fastapi_xml import XmlBody

from llama_cpp import Llama
import datamodel

# app = FastAPI()
app = FastAPI(title="FastAPI::XML", default_response_class=XmlAppResponse)
app.router.route_class = XmlRoute
add_openapi_extension(app)


@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.get("/items/{item_id}")
def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}



# Simple Test
# Du bist ein hilfreicher Java Code Assistent.
# Was ist die Imixs-Workflow engine?

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



@dataclass
class HelloWorld:
    message: str = field(metadata={"example": "Foo","name": "Message", "type": "Element"})



@app.post("/echo", response_model=HelloWorld, tags=["Example"])
def echo(x: HelloWorld = XmlBody()) -> HelloWorld:
    x.message += " For ever!"
    return x