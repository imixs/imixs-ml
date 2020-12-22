from pydantic import BaseModel
import spacy
import random

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


TRAIN_DATA = [('what is the price of polo?', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of ball?', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of jegging?', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of t-shirt?', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of jeans?', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of bat?', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of shirt?', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of bag?', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of cup?', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of jug?', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of plate?', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of glass?', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of moniter?', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of desktop?', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of bottle?', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of mouse?', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of keyboad?', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of chair?', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of table?', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of watch?', {'entities': [(21, 26, 'iban')]})]

 

# The learn method should initialize an empty model and add training data.
#
# See: https://towardsdatascience.com/custom-named-entity-recognition-using-spacy-7140ebbb3718
#         https://medium.com/@manivannan_data/how-to-train-ner-with-custom-training-data-using-spacy-188e0e508c6
#
#
def train(data,iterations):
    
    
    
    # 1.) create blank Language class
    nlp = spacy.blank('en')  
    print("Created blank 'en' model")
    
    
    # 2.) set up the pipeline and entity recognizer.
    if 'ner' not in nlp.pipe_names:
        print("we have no ner so we create an empty one...")
        ner = nlp.create_pipe('ner')
        nlp.add_pipe(ner)
    else:
        pring("we have a ner so we fetch it...")
        ner = nlp.get_pipe('ner')
   
   
   
   
    for i in LABEL:
        print("...adding new label '" + i + "'...")
        ner.add_label(i)   # Add new entity labels to entity recognizer

      # get names of other pipes to disable them during training
    
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe != 'ner']
    with nlp.disable_pipes(*other_pipes):  # only train NER
        optimizer = nlp.begin_training()
        for itn in range(iterations):
            print("Statring iteration " + str(itn))
            random.shuffle(TRAIN_DATA)
            losses = {}
            for text, annotations in TRAIN_DATA:
                nlp.update(
                    [text],  # batch of texts
                    [annotations],  # batch of annotations
                    drop=0.2,  # dropout - make it harder to memorise data
                    sgd=optimizer,  # callable to update weights
                    losses=losses)
            print(losses)
    return nlp


# https://spacy.io/usage/training#example-new-entity-type





# Startup method
if __name__ == "__main__":
    #lerne(TRAIN_DATA)
    prdnlp = train(TRAIN_DATA, 20)
    # Save our trained Model
    modelfile = input("Enter your Model Name: ")
    prdnlp.to_disk(modelfile)
    
    #Test your text
    test_text = input("Enter your testing text: ")
    doc = prdnlp(test_text)
    for ent in doc.ents:
        print(ent.text, ent.start_char, ent.end_char, ent.label_)
