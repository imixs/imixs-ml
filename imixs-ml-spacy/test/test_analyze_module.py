import sys 
sys.path.append('..')

from imixs.core import datamodel, datatrain


"""
The goal of this test program is to demonstrate the usage of the training data module

"""


# Startup method
if __name__ == "__main__":
    
    modelfile = "training_model"
    
    #text = "what is the price of 30,99"
    
    dinges=datamodel.AnalyseData
    dinges.text="what is the price of 30,99"
    
    result=datatrain.analyseText(dinges,modelfile)


    print("result=" , result)
   