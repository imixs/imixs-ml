import os
import random

from pydantic import BaseModel
import spacy
from spacy.util import minibatch, compounding


#
# The goal of this test program is to figure out 
# how we can provide learning data to an existing 
# spaCy model
#
#
#
#
class Data(BaseModel):
    text: str 

# Specify the  categories
LABEL = ['invoice', 'offer', 'order']


# It seems to be important to provide also the False categories to improve the model!

TRAIN_DATA = [('Please see our invoice below.', {'cats': {'invoice': True,'order': False, 'offer': False}}),
              ('Please pay the total amount of our invoice.', {'cats': {'invoice': True,'order': False, 'offer': False}}),
              ('We send you an invoice regarding your 47445', {'cats': {'invoice': True,'order': False, 'offer': False}}),
              
              ('Please send us an offer.', {'cats': {'offer': True, 'invoice': False, 'order': False}}),
              ('Please tell us the price for the parts.', {'cats': {'offer': True, 'invoice': False, 'order': False}}),
              ('Please tell us the price.', {'cats': {'offer': True, 'invoice': False, 'order': False}}),
              
              ('As discussed we would like to order the parts. ', {'cats': {'order': True, 'invoice': False, 'offer': False}}),
              ('We order the parts. ', {'cats': {'order': True, 'invoice': False, 'offer': False}}),
              ('Please send us the parts. ', {'cats': {'order': True, 'invoice': False, 'offer': False}}),
              ('We order the following parts. ', {'cats': {'order': True, 'invoice': False, 'offer': False}})
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
      textcat = nlp.create_pipe("textcat")
      nlp.add_pipe(textcat, last=True) 
    else:
      textcat = nlp.get_pipe("textcat")


    for i in LABEL:
        print("...adding new label '" + i + "'...")
        textcat.add_label(i)   # Add new  labels to the categorizer

    


    # Iterate the training examples to optimize the model
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe != 'textcat']

    # Only train the textcat pipe
    with nlp.disable_pipes(*other_pipes):
        optimizer = nlp.begin_training()
        print("Training model...")
        for i in range(iterations):
            losses = {}
            batches = minibatch(train_data, size=compounding(4.0, 32.0, 1.001))
            for batch in batches:
                texts, annotations = zip(*batch)
                nlp.update(texts, annotations, sgd=optimizer,
                          drop=0.2, losses=losses)

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
    print("test invoice...")
    test_text = "Please see our new invoice below."
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
           
  
    #Test your text
    #test_text = input("Enter your testing text: ")
    print("test order...")
    test_text = "Please send us the parts."
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
    
    
    print("finished")
    
    
    