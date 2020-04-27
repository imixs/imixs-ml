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
# LABEL = ['iban', 'bic', 'invoiceno']

"""
TRAIN_DATA = [('what is the price of polo?', {'entities': [(21, 25, 'iban')]}),
              ('what is the price of ball?', {'entities': [(21, 25, 'iban')]}),
              ('what is the price of jegging?', {'entities': [(21, 28, 'iban')]}),
              ('what is the price of t-shirt?', {'entities': [(21, 28, 'iban')]})]



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
"""

TRAIN_DATA = [('what is the price of 11,00', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 12,50', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 20000,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 10120,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 333,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 0,50', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of 10,40', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 0,49', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of 0,10', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of 9,99', {'entities': [(21, 24, 'iban')]}), 
              ('what is the price of 155,55', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 200,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 90120,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 50000,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 1600,00', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 1500,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 70000,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 30,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 11,50', {'entities': [(21, 26, 'iban')]})] 



# The learn method should initialize an empty model and add training data.
#
# See: https://towardsdatascience.com/custom-named-entity-recognition-using-spacy-7140ebbb3718
#         https://medium.com/@manivannan_data/how-to-train-ner-with-custom-training-data-using-spacy-188e0e508c6
#
#
def train(data, iterations):
    
    model=None
    
    
     
     # 1.) load model or create blank Language class
    """Load the model, set up the pipeline and train the entity recognizer."""
    if model is not None:
        nlp = spacy.load(model)  # load existing spaCy model
        print("Loaded model '%s'" % model)
    else:
        nlp = spacy.blank("en")  # create blank Language class
        print("Created blank 'en' model")
    
    
    # 2.) set up the pipeline and entity recognizer.
    if 'ner' not in nlp.pipe_names:
        print("we have no ner so we create an empty one...")
        ner = nlp.create_pipe('ner')
        nlp.add_pipe(ner)
    else:
        pring("we have a ner so we fetch it...")
        ner = nlp.get_pipe('ner')
        
    # 3.) add the labels contained in the training model...
    for _, annotations in data:
        for ent in annotations.get("entities"):
            
            _label = ent[2]
            _labelList = ner.labels
            # We only need to add the label if it is not already part of the entityRecognizer
            if _label not in _labelList:
                print("...adding new label '" + ent[2] + "'...")
                ner.add_label(ent[2])
      
      
    # get names of other pipes to disable them during training
    pipe_exceptions = ["ner", "trf_wordpiecer", "trf_tok2vec"]
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe not in pipe_exceptions]
    with nlp.disable_pipes(*other_pipes):  # only train NER
        # reset and initialize the weights randomly – but only if we're
        # training a new model
        if model is None:
            nlp.begin_training()
        for itn in range(iterations):
            random.shuffle(TRAIN_DATA)
            losses = {}
            # batch up the examples using spaCy's minibatch
            batches = minibatch(TRAIN_DATA, size=compounding(4.0, 32.0, 1.001))
            for batch in batches:
                texts, annotations = zip(*batch)
                nlp.update(
                    texts,  # batch of texts
                    annotations,  # batch of annotations
                    drop=0.5,  # dropout - make it harder to memorise data
                    losses=losses,
                )
            print("Losses", losses)
    return nlp

# https://spacy.io/usage/training#example-new-entity-type


# Startup method
if __name__ == "__main__":
    # lerne(TRAIN_DATA)
    prdnlp = train(TRAIN_DATA, 20)
    # Save the trained Model
    modelfile = "training_model"
    prdnlp.to_disk(modelfile)
    
    # Test your text
    test_text = input("Enter your testing text: ")
    doc = prdnlp(test_text)
    for ent in doc.ents:
        print(ent.text, ent.start_char, ent.end_char, ent.label_)