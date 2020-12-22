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
print("/___/_/_/_/_//_\_\/___/  V1.1.")
print("")                                           
                                           
print("ENGINE          : https://spacy.io")                                           
print("MODEL_PATH      : " + modelpath)
print("MODEL_LANGUAGE  : " + language)
print("")                                           


@app.post("/training/")
def extract_entities(trainngdata: List[datamodel.TrainingData]):
    # print(">>START trainingdata/")
    prdnlp = datatrain.updateModel(trainngdata, modelpath)
    # print(">>STOP trainingdata/")
    return {"finished"}


@app.post("/training-iterations/")
def extract_entities(trainngdata: List[datamodel.TrainingData]):
    # print(">>START training-single-mode/")
    prdnlp = datatrain.updateModelWithInteration(trainngdata, 10, modelpath)
    # print(">>STOP training-single-mode/")
    return {"finished training-single-mode model"}



# Analyze a text 
#
@app.post("/analyse/")
def train(analyseData: datamodel.AnalyseData):
    result=datatrain.analyseText(analyseData,modelpath)
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




