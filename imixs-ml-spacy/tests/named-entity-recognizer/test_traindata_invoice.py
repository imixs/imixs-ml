import os
import sys
import random
import json
from typing import List
import shutil
# sys import for test only
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from imixs.core import datamodel, modelservice
from spacy import displacy

"""
The goal of this test program is to demonstrate the usage of the training data module

The test builds a traing_data array with test data. 
Next the updateModel method is called 10 times. In each iteration the training data is shuffled 
to avoid a memory effect and simulate a more realistic szeanario. Even in production the training
data will be provided in a random way.  

"""


# Startup method
if __name__ == "__main__":

    # Create or load model    
    modelPath = os.getcwd()+"/imixs-ml-spacy/tests/models/training_model_invoice"   

    # Test if the model exists
    modelExists = os.path.isdir(modelPath)    
    # 1.) load model or create blank Language class 
    if not modelExists:
        nlp = modelservice.initModel(modelPath,"en_core_web_sm")
        #nlp = modelservice.initModel(modelPath,"")

    # Opening JSON file
    datafile = os.getcwd()+"/imixs-ml-spacy/tests/data/test_traindata_invoice.json"
    training_data=datamodel.readTrainingDataFromJSONFile(datafile)

    for _data in training_data:
        print("text=" + _data.text)
        print("wir haben soviel:"+ str(len(_data.entities)))
       
    print("------------start training--------------------")
    for i in range(10):
        print(" ")
        print(i+1,". run....")
        
        random.shuffle(training_data)
        losses = modelservice.updateModel(training_data,modelPath,0.0)
        print("losses: ",losses)
    print("------------training finished--------------------")
                
    # Test your text
    test_text = input("Enter your text to be analyzed: ")
    
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=modelservice.analyseText(anlyseData,modelPath)

    print("result=" , doc)

