from pydantic import BaseModel
import spacy
import random
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

# New entity labels
# Specify the new entity labels which you want to add here
LABEL = ['iban', 'bic', 'invoiceno']


TRAIN_DATA = [('some text about fish', {'cats': {'POSITIVE': 1}}),
              ('some text about birds', {'cats': {'POSITIVE': 0}}),
              ('some text about dogs', {'cats': {'POSITIVE': 0}}),
              ('some text about horses', {'cats': {'POSITIVE': 0}})
             ]
 


# The learn method should initialize an empty model and add training data.
#
# 
#
#
def train(train_data,iterations):
    
    
    
    # 1.) create blank Language class
    nlp = spacy.blank('en')  
    print("Created blank 'en' model")
    
    
    # Initialize a textcat pipe in a spacy pipeline object (nlp), and add the label variable in it.


    if 'textcat' not in nlp.pipe_names:
      print("create pipe 'textcat'...")
      textcat = nlp.create_pipe("textcat")
      nlp.add_pipe(textcat, last=True) 
    else:
      textcat = nlp.get_pipe("textcat")

    textcat.add_label('POSITIVE')



    # Iterate the training examples to optimize the model

    other_pipes = [pipe for pipe in nlp.pipe_names if pipe != 'textcat']

    # Only train the textcat pipe
    with nlp.disable_pipes(*other_pipes):
        optimizer = nlp.begin_training()
        print("Training model...")
        for i in range(iterations):
            losses = {}
            batches = minibatch(train_data, size=compounding(4,32,1.001))
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
    prdnlp = train(TRAIN_DATA, 20)
    
    #prdnlp.to_disk("testmodel")
    
    #Test your text
    #test_text = input("Enter your testing text: ")
    test_text = "some text about fish"
    doc = prdnlp(test_text)
    
    for label, score in doc.cats.items():
        print(label, score)
           
           
    for ent in doc.cats:
        print(ent)    

    
    #for ent in doc.cats:
    #    print(ent.text, ent.start_char, ent.end_char, ent.label_)    
    
    
    
    print("finished")
    
    
    