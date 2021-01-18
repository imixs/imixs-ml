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

modelpath=os.getenv('MODEL_PATH', 'models/')
if (modelpath.endswith('/') == False) :
    modelpath=modelpath+"/"
    
#language=os.getenv('MODEL_LANGUAGE', 'en')



print("   ____      _          ") 
print("  /  _/_ _  (_)_ __ ___  ") 
print(" _/ //  ' \/ /\ \ /(_-<  Machine-Learning"  )
print("/___/_/_/_/_//_\_\/___/  V1.1.")
print("")                                           
                                           
print("ENGINE          : https://spacy.io")                                           
print("MODEL_PATH      : " + modelpath)
#print("MODEL_LANGUAGE  : " + language)
print("")                                           


@app.post("/training/{model}")
def extract_entities(model: str,trainngdata: List[datamodel.TrainingData]):
    # print(">>START trainingdata/")
    prdnlp = datatrain.updateModel(trainngdata, modelpath+model)
    # print(">>STOP trainingdata/")
    return {"finished"}


@app.post("/training-iterations/{model}")
def extract_entities(model: str,trainngdata: List[datamodel.TrainingData]):
    # print(">>START training-single-mode/")
    prdnlp = datatrain.updateModelWithInteration(trainngdata, 10, modelpath+model)
    # print(">>STOP training-single-mode/")
    return {"finished training-single-mode model"}



# Analyze a text 
#
@app.post("/analyse/{model}")
def train(model: str, analyseData: datamodel.AnalyseData):
    print(" anaylse by model: " + model)
    result=datatrain.analyseText(analyseData,modelpath+model)
    return result


# Clean the model 
#
@app.delete("/{model}")
def clean(model: str):
    try:
        # try if this is a valid directory path
        shutil.rmtree(modelpath+model)
    except OSError as e:
        print("Error: %s : %s" % (modelpath+model, e.strerror))
    return {"model '" + modelpath + model +"' deleted"}




