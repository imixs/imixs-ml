import sys 

from imixs.core import datamodel, datatrain

sys.path.append('../..')


#sys.path.append('../..')



"""
The goal of this test program is to demonstrate the usage of the training data module

"""


# Startup method
if __name__ == "__main__":
    
    modelfile = "training_model"
                
    # Test your text
    test_text = input("Enter your testing text: ")
    
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=datatrain.analyseText(anlyseData,modelfile)


    print("result=" , doc)
    
   
   