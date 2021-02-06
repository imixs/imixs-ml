import os
import shutil
from builtins import str
from typing import List
from fastapi import FastAPI, HTTPException
from imixs.core import datamodel, datatrain


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

# Train a training data set
@app.post("/training/{model}")
def extract_entities(model: str,trainngdata: List[datamodel.TrainingData]):
    try : 
        losses = datatrain.updateModel(trainngdata, modelpath+model)
    except Exception as e:
        print (e)
        raise HTTPException(status_code=406, detail="Training Data is not acceptable!") from e    
    return losses



# Analyze a text 
#
@app.post("/analyse/{model}")
def train(model: str, analyseData: datamodel.AnalyseData):
    print(" anaylse by model: " + model)
    try : 
        result=datatrain.analyseText(analyseData,modelpath+model)
    except Exception as e:
        raise HTTPException(status_code=406, detail="failed to analyse data!") from e
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




