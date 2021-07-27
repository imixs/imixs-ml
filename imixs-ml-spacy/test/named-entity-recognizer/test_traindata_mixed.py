import random
import sys 

from imixs.core import datamodel, modelservice

sys.path.append('../..')



"""
The goal of this test program is to demonstrate the usage of the training data module

The test builds a traing_data array with test data. 
Next the updateModel method is called 10 times. In each iteration the training data is shuffled 
to avoid a memory effect and simulate a more realistic szeanario. Even in production the training
data will be provided in a random way.  

"""


# Startup method
if __name__ == "__main__":
    
    # the test model
    modelfile = "models/training_model_mixed"
    
    
    # create training objects
    training_data=[]
    TRAIN_RAWDATA = [
        
              # invoice cats 
              ('what is the price of 11,00', {'entities': [(21, 26, 'price')] , 'cats': [('invoice', True)]}), 
              ('what is the price of 12,50', {'entities': [(21, 26, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 10120,00', {'entities': [(21, 29, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 333,00', {'entities': [(21, 27, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 0,50', {'entities': [(21, 25, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 10,40', {'entities': [(21, 26, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 0,49', {'entities': [(21, 25, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 0,10', {'entities': [(21, 25, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 9,99', {'entities': [(21, 25, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 155,55', {'entities': [(21, 27, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 200,00', {'entities': [(21, 27, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 90120,00', {'entities': [(21, 29, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 50000,00', {'entities': [(21, 29, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 1600,00', {'entities': [(21, 28, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 1500,00', {'entities': [(21, 28, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 70000,00', {'entities': [(21, 29, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 30,00', {'entities': [(21, 26, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 20,00', {'entities': [(21, 26, 'price')], 'cats': [('invoice', True)]}), 
              ('what is the price of 11,50', {'entities': [(21, 26, 'price')], 'cats': [('invoice', True)]}),
              
              # sports cats..
              ('football player shoots a goal', {'entities': [(0,8,'game')], 'cats': [('sports', True)]}), 
              ('football player kicks the ball', {'entities': [(0,8,'game')], 'cats': [('sports', True)]}), 
              ('football ball hits the bar',  {'entities': [(0,8,'game')], 'cats': [('sports', True)]}), 
              ('football player shoots the ball into the side.',  {'entities': [(0,8,'game')], 'cats': [('sports', True)]}),  
              ('football player shoots the ball over the goal.', {'entities': [(0,8,'game')], 'cats': [('sports', True)]}), 
              ('chess player rook hits the pawn.', {'entities': [(0,5,'game')], 'cats': [('sports', True)]}), 
              ('chess player rook hits the bishop.',{'entities': [(0,5,'game')], 'cats': [('sports', True)]}), 
              ('chess player rook hits the queen.', {'entities': [(0,5,'game')], 'cats': [('sports', True)]}), 
              ('chess player rook hits the king.', {'entities': [(0,5,'game')], 'cats': [('sports', True)]}), 
              ('chess player knight hits the pawn.', {'entities': [(0,5,'game')], 'cats': [('sports', True)]}), 
              ('chess player knight hits the bishop.', {'entities': [(0,5,'game')], 'cats': [('sports', True)]}), 
              ('chess player knight hits the queen.', {'entities': [(0,5,'game')], 'cats': [('sports', True)]}), 
              ('chess player knight hits the king.', {'entities': [(0,5,'game')], 'cats': [('sports', True)]})
              
              ,
              ('add some pepper to the soup.', {'entities': [(23,4,'dish')], 'cats': [('cooking', True)]})
              
              ] 
    
    
    
    
    """ 
      Build an array of TraingData objects
    """
    for i in TRAIN_RAWDATA:
        _text=i[0];
        _labels=i[1];
        _entities=_labels.get('entities');
        _cats=_labels.get('cats');
        
        _d=datamodel.TrainingData(text=_text,entities=[],categories=[])
        if _entities is not None:
            for e in _entities:
                _e=datamodel.TrainingEntity(label=e[2],start=e[0],stop=e[1])
                _d.entities.append(_e)
        
        if _cats is not None:
            for c in _cats:
                _c=datamodel.TrainingCategory(label=c[0],enclosed=c[1])
                _d.categories.append(_c)
            
        training_data.append(_d)
        

    print("------------start training--------------------")
    for i in range(10):
        print(" ")
        print(i+1,". run....")
        
        random.shuffle(training_data)
        losses = modelservice.updateModel(training_data,modelfile)
    
    print("------------training finished--------------------")
                
    # Test your text
    test_text = input("Enter your text to be analyzed: ")
    
    
    anlyseData=datamodel.AnalyseData
    anlyseData.text=test_text
    doc=modelservice.analyseText(anlyseData,modelfile)

    print("result=" , doc)



