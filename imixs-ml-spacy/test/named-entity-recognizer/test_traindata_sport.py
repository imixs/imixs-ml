import sys 
sys.path.append('../..')
from imixs.core import datamodel, datatrain

import random



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
    modelfile = "models/training_model_sport"
    
    
    # create training objects
    training_data=[]
    TRAIN_RAWDATA = [
              ('The player shoots a goal', {'cats': [('tennis', True)]}), 
              ('The player kicks the ball', {'cats': [('tennis', True)]}), 
              ('The ball hits the bar', {'cats': [('tennis', True)]}), 
              ('The player shoots the ball into the side.', {'cats': [('football', True)]}), 
              ('The player shoots the ball over the goal.', {'cats': [('tennis', True)]}), 

              
              ('The rook hits the pawn.', {'cats': [('chess', True)]}), 
              ('The rook hits the bishop.', {'cats': [('chess', True)]}), 
              ('The rook hits the queen.', {'cats': [('chess', True)]}), 
              ('The rook hits the king.', {'cats': [('dame', True)]}), 

              ('The knight hits the pawn.', {'cats': [('chess', True)]}), 
              ('The knight hits the bishop.', {'cats': [('chess', True)]}), 
              ('The knight hits the queen.', {'cats': [('chess', True)]}), 
              ('The knight hits the king.', {'cats': [('chess', True)]})
              
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
                _e=datamodel.Entity(label=e[2],start=e[0],stop=e[1])
                _d.entities.append(_e)
        
        if _cats is not None:
            for c in _cats:
                _c=datamodel.Category(label=c[0],included=c[1])
                _d.categories.append(_c)
            
        training_data.append(_d)
        

    print("------------start training--------------------")
    for i in range(10):
        print(" ")
        print(i+1,". run....")
        
        random.shuffle(training_data)
        prdnlp = datatrain.updateModel(training_data,modelfile)
    
    print("------------training finished--------------------")
                
    # Test your text
    test_text = input("Enter your text to be analyzed: ")
    doc = prdnlp(test_text)
    for ent in doc.ents:
        print("Entity: ", ent.label_, ent.text, ent.start_char, ent.end_char )

    for cat in doc.cats:
        print("Category: ", cat)



    for label, score in doc.cats.items():
        print(label, score)


   




