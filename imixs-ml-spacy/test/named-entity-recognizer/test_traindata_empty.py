import sys 
sys.path.append('../..')
from imixs.core import datamodel, modelservice
import random



"""
The goal of this test program is to init an empty model
"""


# Startup method
if __name__ == "__main__":
    
    # the test model
    modelfile = "models/training_model_empty"
    
    modelservice.initModelByCategories([], modelfile)
                  
    # Test your text
    test_text = input("Enter your text to be analyzed: ")
    
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=modelservice.analyseText(anlyseData,modelfile)

    print("result=" , doc)

   




