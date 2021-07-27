import sys 

from imixs.core import datamodel, modelservice

sys.path.append('../..')


"""
The goal of this test program is to demonstrate the usage of the training data module

"""


# Startup method
if __name__ == "__main__":
    
    modelfile = "models/training_model_mixed"
                 
    # Test your text
    print("Example text: 'what is the price of 333,00'   'football player shoots the ball over the goal'")
    test_text = input("Enter your testing text: ")
    
      
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    
    result = modelservice.analyseText(anlyseData,modelfile)


    print("result=" , result)
    
   
   