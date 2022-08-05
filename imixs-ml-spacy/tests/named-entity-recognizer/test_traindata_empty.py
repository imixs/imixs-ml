import os
import sys
import shutil
# sys import for test only
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from imixs.core import datamodel, modelservice

"""
The goal of this test program is to init an empty model
"""


# Startup method
if __name__ == "__main__":

    try:
        # try if this is a valid directory path
        shutil.rmtree(os.getcwd()+"/imixs-ml-spacy/models/training_model_empty")
    except OSError as e:
        print("Error: %s : %s" % (modelpath+model, e.strerror))
    # the test model
    modelfile = os.getcwd()+"/imixs-ml-spacy/models/training_model_empty"    
    modelservice.initModel( modelfile)
                  
    # Test your text
    test_text = input("Enter your text to be analyzed: ")
    
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=modelservice.analyseText(anlyseData,modelfile)

    print("result=" , doc)

   




