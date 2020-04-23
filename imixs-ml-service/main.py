from fastapi import FastAPI
from pydantic import BaseModel
import spacy

nlp_en = spacy.load("en_core_web_sm")
app = FastAPI()


class Data(BaseModel):
    text: str 


TRAIN_DATA = [
    (
        "Horses are too tall and they pretend to care about your feelings",
        {"entities": [(0, 6, "MY_ANIMAL")]},
    ),
    ("Do they bite or bark?", {"entities": []}),
    (
        "horses are too tall and they pretend to care about your feelings",
        {"entities": [(0, 6, "MY_ANIMAL")]},
    ),
] 
 

@app.post("/text/")
def extract_entities(data: Data, lang: str):
    doc_en = nlp_en(data.text)
    ents = []
    for ent in doc_en.ents:
        ents.append({"text": ent.text, "label_": ent.label_})
    return {"message": data.text, "lang": lang, "ents": ents}


# Ein Versuch
#
# Quelle: https://towardsdatascience.com/custom-named-entity-recognition-using-spacy-7140ebbb3718
#         https://medium.com/@manivannan_data/how-to-train-ner-with-custom-training-data-using-spacy-188e0e508c6
#
#
@app.post("/ralpus/")
def lerne(data: Data):
    
    # 1.) create blank Language class
    nlp = spacy.blank('en')  
    print("Created blank 'en' model")
    
    
    # 2.) set up the pipeline and entity recognizer.
    if 'ner' not in nlp.pipe_names:
        ner = nlp.create_pipe('ner')
        nlp.add_pipe(ner)
    else:
        ner = nlp.get_pipe('ner')
   
   
   
    # 3.) The training data.....
    print("Muster DATA:")
    print(TRAIN_DATA)
    
    
    print("Traning Data:")
    print(data.text)
     
    #training_data = []
    #entities = []
    #training_data.append(("das ist mein text", {"entities" : entities}))
    
   
    
    
    #doc_en = nlp(data.text)
    doc_en = nlp(TRAIN_DATA)
    ents = []
    for ent in doc_en.ents:
        ents.append({"text": ent.text, "label_": ent.label_})
    return {"message": data.text, "lang": lang, "ents": ents}

# https://spacy.io/usage/training#example-new-entity-type

