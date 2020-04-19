from pydantic import BaseModel
import spacy

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
 

# The learn method should initialize an empty model and add training data.
#
# See: https://towardsdatascience.com/custom-named-entity-recognition-using-spacy-7140ebbb3718
#         https://medium.com/@manivannan_data/how-to-train-ner-with-custom-training-data-using-spacy-188e0e508c6
#
#
def lerne(data: Data):
    
    # 1.) create blank Language class
    nlp = spacy.blank('en')  
    print("1Created blank 'en' model")
    
    
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
    print(data)
     
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





# Startup method
if __name__ == "__main__":
    lerne(TRAIN_DATA)

