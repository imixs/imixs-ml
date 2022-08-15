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
 
               
    # Read Test File
    with open(os.getcwd()+"/imixs-ml-spacy/tests/data/sample-001.txt", "r") as file:
        test_text = file.read()
    
    nlp=spacy.load("en_core_web_sm")
    doc = nlp(test_text)

    # display result with spacy Display
    displacy.serve(doc, style="ent")

   




