from pydantic import BaseModel
import spacy
import random
import os
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
LABEL = ['fussball', 'schach']



TRAIN_DATA = [
              
              ('Er schießt mit dem Ball ein Tor', {'cats': {'fussball': True}}),
              ('Er scheißt den Ball ins Abseits', {'cats': {'fussball': True}}),
              ('Er scheißt den Ball ins Seitenaus', {'cats': {'fussball': True}}),
              ('Er scheißt den Ball neben das Tor', {'cats': {'fussball': True}}),
              ('Er scheißt den Ball über das Tor', {'cats': {'fussball': True}}),
              ('Er scheißt den Ball an den Balken', {'cats': {'fussball': True}}),
              ('Er köpft den Ball ins Tor', {'cats': {'fussball': True}}),
              ('Er köpft den Ball ins Abseits', {'cats': {'fussball': True}}),
              ('Er köpft den Ball ins Seitenaus', {'cats': {'fussball': True}}),
              ('Er köpft den Ball an den Balken', {'cats': {'fussball': True}}),
              ('Er köpft den Ball ins Tor', {'cats': {'fussball': True}}),
              
              ('Er zieht mit den Läufer', {'cats': {'schach': True}}),
              ('Er zieht mit den Turm', {'cats': {'schach': True}}),
              ('Er zieht mit dem Bauer', {'cats': {'schach': True}}),
              ('Er zieht mit dem König', {'cats': {'schach': True}}),
              ('Er schlägt die Dame', {'cats': {'schach': True}}),
              ('Er schlägt den König', {'cats': {'schach': True}}),
              ('Er schlägt den Turm', {'cats': {'schach': True}}),
              ('Er schlägt den Bauer', {'cats': {'schach': True}}),
              ('Er schlägt den Läufer', {'cats': {'schach': True}})
              
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
                          #drop=0.2, 
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
    print("=== testing Fussball...")
    test_text = "Er schießt mit dem Ball ein Tor"
    #test_text = "Aus dem Abseits rollt der Ball ins Tor"
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
           
  
    #Test your text
    #test_text = input("Enter your testing text: ")
    print("=== testing Schach...")
    test_text = "Er zieht mit dem König"
    #test_text = "Er setzt den König mit der Dame Schachmatt"
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
    
    
    print("finished")
    
    