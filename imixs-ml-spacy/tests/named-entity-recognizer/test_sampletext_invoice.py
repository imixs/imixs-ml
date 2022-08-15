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
import spacy
"""
This test simply tests an invoice text for a given model and 
shows the result in the SpaCy display
"""


# Startup method
if __name__ == "__main__":

    # Create or load model    
    #modelPath = os.getcwd()+"/imixs-ml-spacy/models/invoice-de-0.2.0"   
    modelPath = os.getcwd()+"/imixs-ml-spacy/tests/models/training_model_invoice" 
               
    # Read Test File
    with open(os.getcwd()+"/imixs-ml-spacy/tests/data/sample-001.txt", "r") as file:
        test_text = file.read()
    
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=modelservice.analyseText(anlyseData,modelPath)

    # display result with spacy Display
    displacy.serve(doc, style="ent")

   




