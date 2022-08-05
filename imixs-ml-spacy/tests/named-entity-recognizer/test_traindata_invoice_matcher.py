import os
import sys
import random
import json
from typing import List
import shutil
# sys import for test only
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from imixs.core import datamodel, modelservice


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
    modelPath = os.getcwd()+"/imixs-ml-spacy/models/training_model_invoice_matcher"   



#lesen ! https://github.com/explosion/spaCy/issues/5727

    # Test if the model exists
    modelExists = os.path.isdir(modelPath)    
    # 1.) load model or create blank Language class 
    if not modelExists:
        nlp = modelservice.initModel(modelPath)

    # Opening JSON file
    datafile = os.getcwd()+"/imixs-ml-spacy/tests/data/test_traindata_invoice_matcher.json"
    training_data=datamodel.readTrainingDataFromJSONFile(datafile)


    # Analyze trainingdata
    for _data in training_data:
        _text=_data.text
        print("text=" + _data.text) 
        for _e in _data.entities:
          print(_e.label + "="+_text[_e.start:_e.stop])
          
       
    print("------------start training--------------------")
    for i in range(10):
        print(" ")
        print(i+1,". run....")
        
        random.shuffle(training_data)
        losses = modelservice.updateModel(training_data,modelPath,0.0)
        print("losses:",losses)
    
    print("------------training finished--------------------") 
                
    # Test your text
    #test_text = input("Enter your text to be analyzed: ")
    #test_text="Die Demo GmbH   Blumen Str. 112  80123 München  Ust. ID Nr. WRONDEBB991 Datum: 01.08.2022 Kunden Nr.: 00627 Rechnungssummer: EUR 111,11  IBAN: AT11 111 221 333 000 042 999 / BIC: IMSXDEBBAAC, Alternativ beachten Sie bitte unsere ander BIC IMIXDEBBAA1 oder IMIXDEBBAA2 Grüße.."
    test_text="Die Imixs Software Solutions GbmH ist eine schöne Firma. 80123 München  Ust. ID Nr. WRONDEBB091 Datum: 01.08.2022 Kunden Nr.: 00627 Rechnungssummer: EUR 111,11  IBAN: AT11 111 221 333 000 042 999 / BIC: IMSXDEBBAAC, Alternativ beachten Sie bitte unsere ander BIC IMIXDEBBAA1 oder IMIXDEBBAA9 Grüße.."
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=modelservice.analyseText(anlyseData,modelPath)
    print("result=" , doc)

   




