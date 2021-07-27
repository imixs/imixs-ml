import os
import random

from pydantic import BaseModel

import spacy
from spacy.util import minibatch, compounding


#
# The goal of this test program is to figure out 
# how we can provide learning data to an empty 
# spaCy model
#
#
#
#
class Data(BaseModel):
    text: str 

# Specify the  categories
LABEL = ['football', 'chess']



TRAIN_DATA = [
              
              ('He kicks the ball into the goal', {'cats': {'football': True}}),
              ('He kicks the ball into the offside', {'cats': {'football': True}}),
              ('He kicks the ball out of bounds', {'cats': {'football': True}}),
              ('He shoots the ball next to the goal', {'cats': {'football': True}}),
              ('He shoots the ball above the goal', {'cats': {'football': True}}),
              ('He shoots the ball on the beam', {'cats': {'football': True}}),
              ('He heads the ball into the goal', {'cats': {'football': True}}),
              ('He heads the ball into the offside', {'cats': {'football': True}}),
              ('He heads the ball out of bounds', {'cats': {'football': True}}),
              ('He heads the ball into the goal', {'cats': {'football': True}}),
              ('He heads the ball above the goal', {'cats': {'football': True}}),
              
              ('He moves with the bishop', {'cats': {'chess': True}}),
              ('He moves with the rook', {'cats': {'chess': True}}),
              ('He moves with the king', {'cats': {'chess': True}}),
              ('He moves with the queen', {'cats': {'chess': True}}),
              ('He moves with the knight', {'cats': {'chess': True}}),
              ('He moves with the pawn', {'cats': {'chess': True}}),
              ('She beats the king', {'cats': {'chess': True}}),
              ('She beats the bishop', {'cats': {'chess': True}}),
              ('She beats the rook', {'cats': {'chess': True}}),
              ('She beats the queen', {'cats': {'chess': True}}),
              ('She beats the knight', {'cats': {'chess': True}}),
              ('She beats the king', {'cats': {'chess': True}}),
              ('She beats the pawn', {'cats': {'chess': True}}),
              ('He checksmate the king with the queen', {'cats': {'chess': True}})
              
             ]

# The learn method should initialize an empty model and add training data.
#
# 
#
#
def train(train_data,iterations,modelPath):
    
    # Test if the model exists
    modelExists=os.path.isdir(modelPath)
     
     # 1.) load model or create blank Language class
    """Load the model, set up the pipeline and train the entity recognizer."""
    if modelExists:
        nlp = spacy.load(modelPath)  # load existing spaCy model
        print("Loaded model '%s'" % modelPath)
    else:
        # 1.) create blank Language class
        nlp = spacy.blank("en")  # create blank Language class
        print("Created blank 'en' model")
    

    
    
    # Initialize a textcat pipe in a spacy pipeline object (nlp), and add the label variable in it.


    if 'textcat' not in nlp.pipe_names:
      print("create pipe 'textcat'...")
      textcat = nlp.create_pipe("textcat", config={"exclusive_classes": False})
      #textcat = nlp.create_pipe("textcat")
      nlp.add_pipe(textcat, last=True) 
    else:
      textcat = nlp.get_pipe("textcat")


    for i in LABEL:        
        if i not in textcat.labels:
            print("...adding new label '" + i + "'...")  
            textcat.add_label(i)   # Add new  labels to the categorizer

    


    # Iterate the training examples to optimize the model
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe != 'textcat']

    # Only train the textcat pipe
    with nlp.disable_pipes(*other_pipes):
        
                # reset and initialize the weights randomly – but only if we're
        # training a new model
        if not modelExists:
            print("begin new training!")
            optimizer=nlp.begin_training()
        
        
        
        #optimizer = nlp.begin_training()
        print("Training model...")
        for i in range(iterations):
            losses = {}
            batches = minibatch(train_data, size=compounding(4.0, 32.0, 1.001))
            for batch in batches:
                texts, annotations = zip(*batch)
                nlp.update(texts, annotations,
                          #sgd=optimizer,
                          #drop=0.5, 
                          losses=losses)



            print(losses)
    print("Training completed")
    return nlp



# Startup method
if __name__ == "__main__":
    #lerne(TRAIN_DATA)
    prdnlp = train(TRAIN_DATA, 20,"training_model")
    # save the model...
    prdnlp.to_disk("training_model")
    
    #Test your text
    #test_text = input("Enter your testing text: ")
    print("=== testing football...")
    test_text = "He heads the ball above the goal"
    #test_text = "Aus dem Abseits rollt der Ball ins Tor"
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
           
  
    #Test your text
    #test_text = input("Enter your testing text: ")
    print("=== testing chess...")
    test_text = "He moves with the queen"
    #test_text = "Er setzt den König mit der Dame chessmatt"
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
    
    
    print("finished")
    
    