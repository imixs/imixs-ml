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

# New entity labels
# Specify the new entity labels which you want to add here
LABEL = ['iban', 'bic', 'invoiceno']


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
def train(data: Data):
    
    
    
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

    if model is None:
        print("model is none - set begin_traing...")
        optimizer = nlp.begin_training()
    else:
        print("model exists - create optimizer")
        optimizer = nlp.entity.create_optimizer()

   
   
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
    train(TRAIN_DATA)

