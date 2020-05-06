import os
import spacy
import random
import shutil

from fastapi import FastAPI
from spacy.util import minibatch, compounding
from imixs.core import datamodel, datatrain
from typing import List
from pydantic import BaseModel
from builtins import str


nlp_en = spacy.load("en_core_web_sm")
app = FastAPI()






@app.post("/textlist/")
def extract_entities(trainngdata: List[datamodel.TrainingData]):
    print("training data length=" + str(len(trainngdata)))
    prdnlp = datatrain.updateModel(trainngdata, 10,"some-other-model")
    print("------------finished--------------------")
    return {"fertig"}







# Analyze a text 
#
@app.post("/analyze/")
def train(text: str):
  
    result=datatrain.analyzeText(text,"some-other-model")

    #return {"result": result}
    
    return result




# Clean the model 
#
@app.delete("/model/")
def clean():
  
    try:
        shutil.rmtree("some-other-model")
    except OSError as e:
        print("Error: %s : %s" % ("some-other-model", e.strerror))

    return {"model deleted"}

