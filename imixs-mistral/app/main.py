from typing import Union
from dataclasses import dataclass, field
from fastapi import FastAPI
from fastapi_xml import add_openapi_extension
from fastapi_xml import XmlRoute
from fastapi_xml import XmlAppResponse
from fastapi_xml import XmlBody

from llama_cpp import Llama
from imixs.core import datamodel

# Setup FastAPI with the default XMLAPPResponse class
# 
# app = FastAPI()
app = FastAPI(title="FastAPI::XML", default_response_class=XmlAppResponse)
app.router.route_class = XmlRoute
add_openapi_extension(app)



#####################
# Basis prompt method. This method expects a XMLPrompt dataobject holding the system and user message
# The output is stored in the tag 'output'.
#
# Example: 
# <XMLPrompt>
#	<system_message>Du bist ein hilfreicher Java Code Assistent.</system_message>
#	<user_message>Was ist die Imixs-Workflow engine?</user_message>
#   <output></output>
# </XMLPrompt>
#
@app.post("/prompt", response_model=datamodel.XMLPrompt, tags=["Imixs-AI"])
def prompt(x: datamodel.XMLPrompt = XmlBody()) -> datamodel.XMLPrompt:


    # Model parameters
    max_tokens = 2000
    model_path = "/models/mistral-7b-instruct-v0.2.Q4_K_M.gguf"

    # Create a llama model
    model = Llama(
        model_path=model_path,
        temperature=0.75,
        max_tokens=2000,
        n_ctx=2048,
        top_p=1,    
    )

    prompt = f"""<s>[INST] <<SYS>>{x.system_message}<</SYS>>{x.user_message} [/INST]"""
    print("start processing prompt:\n\n",prompt,'\n...\n')
    x.output = model(prompt, max_tokens=max_tokens, echo=True)

    return x;

