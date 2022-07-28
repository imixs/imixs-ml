import os
import random

import spacy
from spacy.util import minibatch, compounding

from pydantic import BaseModel


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


# Test data with True cats only


# In this example we provide only one category per test row.
# This needs longer trainging as if the false categories are provided directly per each row

#set exclusive_classes to False as we provide only on category per test row.
# See: https://spacy.io/api/textcategorizer#init

TRAIN_DATA = [('Please see our invoice below. Total 300,00 EUR IBAN 3355BBBAAAXXX  invoice date 2020-03-03', {'cats': {'invoice': True}}),
              ('Please see our invoice below. Total 40,00 EUR IBAN 3355BB444AXXX  invoice date 2021-03-03', {'cats': {'invoice': True}}),
              #('We send you an invoice regarding your 47445', {'cats': {'invoice': True}}),
              
              ('Please send us an offer. 300 plates, 20 tables, 100 chairs  date 2020-05-01', {'cats': {'offer': True}}),
              ('Please send us an offer. 300 tables, 20 chairs  date 2021-05-01', {'cats': {'offer': True}}),
              #('Please tell us the price for the parts.', {'cats': {'offer': True}}),
              #('Please tell us the price.', {'cats': {'offer': True}}),
              
              #('As discussed we would like to order the parts. ', {'cats': {'order': True}}),
              #('We order the parts. ', {'cats': {'order': True}}),
              #('Please send us the parts. ', {'cats': {'order': True}}),
              ('We order the following parts. order-no 1145 delivery date 2020-05-01', {'cats': {'order': True}})
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
      
      # create a new pipe with exclusive_classes=true
      
      #textcat = nlp.create_pipe("textcat")
      textcat = nlp.create_pipe("textcat", config={"exclusive_classes": True})
      nlp.add_pipe(textcat, last=True)
      #nlp.add_pipe(textcat) 
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
        
        #optimizer = nlp.begin_training(component_cfg={'textcat': {"exclusive_classes": False} })

        
        print("Training model...")
        for i in range(iterations):
            random.shuffle(train_data)
            losses = {}
            batches = minibatch(train_data, size=compounding(4.0, 32.0, 1.001))
            for batch in batches:
                texts, annotations = zip(*batch)
                nlp.update(texts, 
                           annotations, 
                           sgd=optimizer,
                           drop=0.2, 
                           losses=losses)

            print("Losses", losses)
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
    print("=== testing invoice...")
    test_text = "Please see our invoice below. Total 300,00 EUR IBAN 3355BBBAAAXXX  invoice date 2020-03-03"
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
           
  
    #Test your text
    #test_text = input("Enter your testing text: ")
    print("=== testing order...")
    test_text = "We order the following parts. order-no 1145 delivery date 2020-05-01"
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
    
    
    print("finished")
    
    
    