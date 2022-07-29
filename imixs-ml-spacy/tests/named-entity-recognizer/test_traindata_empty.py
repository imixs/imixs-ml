import os
import sys 
# sys import for test only
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from imixs.core import datamodel, modelservice






"""
The goal of this test program is to init an empty model
"""


# Startup method
if __name__ == "__main__":
    
    # the test model
    modelfile = "models/training_model_empty"
    
    modelservice.initModel( modelfile)
                  
    # Test your text
    test_text = input("Enter your text to be analyzed: ")
    
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=modelservice.analyseText(anlyseData,modelfile)

    print("result=" , doc)

   




