from builtins import str
import os
import shutil
from typing import List

from fastapi import FastAPI, HTTPException

from imixs.core import datamodel, modelservice


app = FastAPI()

modelpath=os.getenv('MODEL_PATH', 'models/')
if (modelpath.endswith('/') == False) :
    modelpath=modelpath+"/"
    

print("   ____      _          ") 
print("  /  _/_ _  (_)_ __ ___  ") 
print(" _/ //  ' \/ /\ \ /(_-<  Machine-Learning"  )
print("/___/_/_/_/_//_\_\/___/  V1.1")
print("")                                           
                                           
print("ENGINE          : https://spacy.io")                                           
print("MODEL_PATH      : " + modelpath)
print("")                                           

# Train a training data set
@app.post("/training/{model}")
def train_model(model: str,trainngdata: List[datamodel.TrainingData]):
    try : 
        losses = modelservice.updateModel(trainngdata, modelpath+model)
    except Exception as e:
        print (e)
        raise HTTPException(status_code=406, detail="Training Data is not acceptable!") from e    
    return losses



# Analyze a text 
#
@app.post("/analyse/{model}")
def analyse_text(model: str, analyseData: datamodel.AnalyseData):
    print("anaylse by model: " + model)
    try : 
        result=modelservice.analyseText(analyseData,modelpath+model)
    except Exception as e:
        raise HTTPException(status_code=406, detail="failed to analyse data!") from e
    return result




# Initialize a new model with a given set of categories 
#
@app.post("/{model}")
def init_Model(model: str):
    try:
        # try to create a blank model
        result=modelservice.initModel(modelpath+model)
        return result
    except Exception as e:
        print("failed to init model '" + modelpath + model +"' ")
        raise HTTPException(status_code=406, detail="failed to init model '" + modelpath + model +"' ") from e
    return {}


# Deletes an existing model 
#
@app.delete("/{model}")
def delete_Model(model: str):
    try:
        # try if this is a valid directory path
        return shutil.rmtree(modelpath+model)
    except OSError as e:
        print("Error: %s : %s" % (modelpath+model, e.strerror))
    return {"model '" + modelpath + model +"' created"}


# Health endpoint returns status:UP in case models are defined.
# The method returns status:DOWN of no models exists or a IO error occurred  
#
@app.get("/health")
def health_Check():
    try:
        directory_contents = os.listdir(modelpath)
        return {"status":"UP","models":directory_contents}
    except Exception as e:
        result={"status":"DOWN","exception":format(e)}
        raise HTTPException(status_code=503, detail=result) from e
    return {"status":"DOWN"}

