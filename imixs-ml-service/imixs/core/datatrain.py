import spacy
import random
import time
import os

from spacy.util import minibatch, compounding
from imixs.core import datamodel

"""
This module provides a training method to create or update a model.

The method train() verifies if a model already exists and loads it in this case. 


@author: ralph.soika@imixs.com
@version:  1.0
"""


# This method expects a training data set and updates the given model.
#     
def updateModel(trainingDataSet, iterations, modelPath):
    
    
    # Analyze the data object ......
    for _data in trainingDataSet:
        entity=_data.entities[0]
        ausschnitt=_data.text[entity.start:entity.stop]
        print(_data.text + "  ==>>  "+entity.label + "=" + ausschnitt)
        
        
    
    # Test if the model exists
    modelExists=os.path.isdir(modelPath)
    modelExists=False 
     # 1.) load model or create blank Language class
    """Load the model, set up the pipeline and train the entity recognizer."""
    if modelExists:
        nlp = spacy.load(modelPath)  # load existing spaCy model
        print("Loaded model '%s'" % modelPath)
    else:
        nlp = spacy.blank("en")  # create blank Language class
        print("Created blank 'en' model")
    
    
  
    
    # 2.) set up the pipeline and entity recognizer.
    if 'ner' not in nlp.pipe_names:
        print("we have no ner so we create an empty one...")
        ner = nlp.create_pipe('ner')
        nlp.add_pipe(ner)
    else:
        print("we have a ner so we fetch it...")
        ner = nlp.get_pipe('ner')
        
        
        
    # 3.) add the labels contained in the trainingDataSet...
    for _data in trainingDataSet:
        for ent in _data.entities:
            _label = ent.label
            _labelList = ner.labels
            # We only need to add the label if it is not already part of the entityRecognizer
            if _label not in _labelList:
                print("...adding new label '" + _label + "'...")
                ner.add_label(_label)

      
    # get names of other pipes to disable them during training
    pipe_exceptions = ["ner", "trf_wordpiecer", "trf_tok2vec"]
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe not in pipe_exceptions]
        
        

    lMilis = int(round(time.time() * 1000))
    with nlp.disable_pipes(*other_pipes):  # only train NER
        # reset and initialize the weights randomly – but only if we're
        # training a new model
        if not modelExists:
            nlp.begin_training()

        # Convert the data list to the Spacy Training Data format
        trainingData=datamodel.convertToTrainingData(trainingDataSet)

        for itn in range(iterations):
            random.shuffle(trainingData)
            losses = {}
            
            # batch up the examples using spaCy's minibatch which is much faster than 
            batches = minibatch(trainingData, size=compounding(4.0, 32.0, 1.001))
            for batch in batches:
                texts, annotations = zip(*batch)
                nlp.update(
                    texts,  # batch of texts
                    annotations,  # batch of annotations
                    drop=0.5,  # dropout - make it harder to memorise data
                    losses=losses,
                )            
            
            """
            for text, annotations in trainingData:
                nlp.update([text], [annotations], drop=0.5,losses=losses)
            """
            
            print("Losses", losses)
            
            
    print("total time = "+str(int(round(time.time() * 1000))-lMilis) + "ms  ")        
  
    # finally we save the updated model to disk
    print("save model to disk "+modelPath)
    nlp.to_disk(modelPath)
  
    return nlp
