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
print("/___/_/_/_/_//_\_\/___/  V0.1a")
print("")                                           
                                           
print("ENGINE          : https://spacy.io")                                           
print("MODEL_PATH      : " + modelpath)
print("MODEL_LANGUAGE  : " + language)
print("")                                           


@app.post("/trainingdata/")
def extract_entities(trainngdata: List[datamodel.TrainingData]):
    print(">>START trainingdata/")
    prdnlp = datatrain.updateModel(trainngdata, 10,modelpath)
    print(">>STOP trainingdata/")
    return {"finished"}


@app.post("/trainingdatasingle/")
def extract_entities(trainngdata: List[datamodel.TrainingData]):
    print(">>START trainingdatasingle/")
    prdnlp = datatrain.updateModelSingle(trainngdata, modelpath)
    print(">>STOP trainingdatasingle/")
    return {"finished single model"}



# Analyze a text 
#
@app.post("/analyze/")
def train(analyzeData: datamodel.AnalyzeData):
    result=datatrain.analyzeText(analyzeData,modelpath)
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




