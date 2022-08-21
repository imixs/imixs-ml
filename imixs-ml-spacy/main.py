import os
import sys
import shutil
import logging
from logging.handlers import TimedRotatingFileHandler

from builtins import str
from fastapi import FastAPI, HTTPException
from imixs.core import datamodel, modelservice
from typing import List

# Setup Logging
# See: https://stackoverflow.com/questions/13733552/logger-configuration-to-log-to-file-and-print-to-stdout
#      https://stackoverflow.com/questions/40088496/how-to-use-pythons-rotatingfilehandler
#
# We rotate 5 logfiles over the last 7 days
logging.basicConfig(
        handlers=[TimedRotatingFileHandler('./imixs.log', when='D', interval=1, backupCount=7)],
        level=logging.INFO,
        format="[%(asctime)s] %(levelname)s [%(name)s.%(funcName)s:%(lineno)d] %(message)s",
        datefmt='%Y-%m-%dT%H:%M:%S')

logger = logging.getLogger('imixs-ml')
print = logger.info
app = FastAPI()

modelpath=os.getenv('MODEL_PATH', 'models/')
if (modelpath.endswith('/') == False) :
    modelpath=modelpath+"/"
    
# Print header into stout
print("   ____      _          ") 
print("  /  _/_ _  (_)_ __ ___  ") 
print(" _/ //  ' \/ /\ \ /(_-<  Machine-Learning"  )
print("/___/_/_/_/_//_\_\/___/  V1.6")
print("")                                           
print("ENGINE          : https://spacy.io")                                           
print("MODEL_PATH      : " + modelpath)
print("LOG FILE        : ./imixs.log")
print("")                                           
print("")                                           
                                        
logging.info("modelpath="+modelpath)

# Train a training data set
@app.post("/training/{model}")
def train_model(model: str,trainngdata: List[datamodel.TrainingData],min_losses: float = 0.0, retrain_rate: int = 25):
    try : 
        if min_losses==0.0:
            # optional read min_losses from environment variables
            min_losses = float(os.getenv('MIN_LOSSES', '0.0'))

        losses = modelservice.updateModel(trainngdata, modelpath+model,min_losses,retrain_rate)
    except Exception as e:
        print (e)
        raise HTTPException(status_code=406, detail="Training Data is not acceptable!") from e    
    return losses

# validates a training data set without updating the model
@app.post("/validate/{model}")
def validate_model(model: str,trainngdata: List[datamodel.TrainingData]):
    try : 
        losses = modelservice.validateModel(trainngdata, modelpath+model)
    except Exception as e:
        print (e)
        raise HTTPException(status_code=406, detail="Training Data is not acceptable!") from e    
    return losses

# Analyze a text 
#
@app.post("/analyse/{model}")
def analyse_text(model: str, analyseData: datamodel.AnalyseData):
    logger.info("anaylse by model: " + model)
    try : 
        doc=modelservice.analyseText(analyseData,modelpath+model)
        result=modelservice.getResultData(doc)
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
        logger.info("failed to init model '" + modelpath + model +"' ")
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
        logger.info("Error: %s : %s" % (modelpath+model, e.strerror))
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

