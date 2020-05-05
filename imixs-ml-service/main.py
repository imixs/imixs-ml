#from typing import List
from fastapi import FastAPI
#from pydantic import BaseModel
import spacy
from spacy.util import minibatch, compounding

from imixs.core import datamodel, datatrain

from pydantic import BaseModel
import random


nlp_en = spacy.load("en_core_web_sm")
app = FastAPI()



@app.post("/text/")
def extract_entities(data: datamodel.TrainingData):
    
    print("ich versuche den text zu lesen")
    print(data.text)
    
    print("jetzt lesen wir die entiteis...")
    
    entities=data.entities
    
   
    for ent in entities:
       print("ich versuche entities zu lesen")
       
       print("label=" + ent.label)
           
               
    doc_en = nlp_en(data.text)
    ents = []
    for ent in doc_en.ents:
        ents.append({"text": ent.text, "label_": ent.label_})
    return {"message": data.text, "lang": "en", "ents": ents}


# Ein Versuch
#
# Quelle: https://towardsdatascience.com/custom-named-entity-recognition-using-spacy-7140ebbb3718
#         https://medium.com/@manivannan_data/how-to-train-ner-with-custom-training-data-using-spacy-188e0e508c6
#
#
@app.post("/ralpus/")
def train(data: datamodel.TrainingData, iterations: int):
  
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

    for ent in data.entities:
        
        _label = ent.label
        print("hurrar wir haben ein label: "+_label)
        _labelList = ner.labels
        # We only need to add the label if it is not already part of the entityRecognizer
        if _label not in _labelList:
            print("...adding new label '" + _label + "'...")
            ner.add_label(_label)
      
      
    # get names of other pipes to disable them during training
    pipe_exceptions = ["ner", "trf_wordpiecer", "trf_tok2vec"]
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe not in pipe_exceptions]
    with nlp.disable_pipes(*other_pipes):  # only train NER
        # reset and initialize the weights randomly – but only if we're
        # training a new model
        if model is None:
            nlp.begin_training()
        for itn in range(iterations):
            random.shuffle(data)
            losses = {}
            # batch up the examples using spaCy's minibatch
            batches = minibatch(data, size=compounding(4.0, 32.0, 1.001))
            for batch in batches:
                texts, annotations = zip(*batch)
                nlp.update(
                    texts,  # batch of texts
                    annotations,  # batch of annotations
                    drop=0.5,  # dropout - make it harder to memorise data
                    losses=losses,
                )

            print("Losses", losses)

    return {"message": data.text}

