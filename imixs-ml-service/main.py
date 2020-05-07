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


app = FastAPI()

modelpath=os.getenv('MODEL_PATH', 'imixs-model')
language=os.getenv('MODEL_LANGUAGE', 'en')



print("   ____      _          ") 
print("  /  _/_ _  (_)_ __ ___  ") 
print(" _/ //  ' \/ /\ \ /(_-<  Machine-Learning"  )
print("/___/_/_/_/_//_\_\/___/  V0.1")
print("")                                           
                                           
print("ENGINE          : https://spacy.io")                                           
print("MODEL_PATH      : " + modelpath)
print("MODEL_LANGUAGE  : " + language)
print("")                                           


@app.post("/trainingdata/")
def extract_entities(trainngdata: List[datamodel.TrainingData]):
    prdnlp = datatrain.updateModel(trainngdata, 10,modelpath)
    return {"finished"}



# Analyze a text 
#
@app.post("/analyze/")
def train(text: str):
    result=datatrain.analyzeText(text,modelpath)
    return result


# Clean the model 
#
@app.delete("/model/")
def clean():
    try:
        shutil.rmtree(modelpath)
    except OSError as e:
        print("Error: %s : %s" % (modelpath, e.strerror))
    return {"model '" + modelpath + "' deleted"}




