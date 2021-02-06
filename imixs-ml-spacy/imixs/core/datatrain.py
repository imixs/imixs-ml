"""
This module provides methods to train a model or to analyse text on an 
existing model. 

The method 'updateModel expects a training datamodel. The implementation is based on 
the concept of 'incremental training'. Typically the method expects only one training document,
but also a list of training objects will be processed.


@see https://spacy.io/api/language#update
@author: ralph.soika@imixs.com
@version:  2.0
"""
    

import os
import spacy
from spacy.training import Example
from imixs.core import datamodel




"""
 This method 'updateModel' expects a training data set containing one or many training objects.
 The method updates the given model. If no model exists the method will create a new one.
 
 The method did not use the minibatch algorithm provided by spacy because the assumption is that 
 only one document is trained in a 'incremental training mode' on each call.
 
""" 
def updateModel(trainingDataSet, modelPath):
    
    print("updateModel....")
    
    # Read language
    language=os.getenv('MODEL_LANGUAGE', 'en')
    
    # Analyse the data object ......
    hasEntities = False
    hasCategories = False
    restartTraining = False
    
    for _data in trainingDataSet:
        if (len(_data.entities)>0):
            hasEntities = True
        if (len(_data.categories)>0):
            hasCategories = True
        
    
    # Test if the model exists
    modelExists=os.path.isdir(modelPath)
    
    # 1.) load model or create blank Language class 
    """Load the model, set up the pipeline and train the entity recognizer."""
    if modelExists:
        nlp = spacy.load(modelPath)  # load existing spaCy model
        # print("Loaded model '%s'" % modelPath)
    else:
        nlp = spacy.blank(language)  # create blank Language class
        restartTraining = True
        print("...creating blank model, language='" +language + "'")
    
    
    # 2.) set up the pipeline and entity recognizer.
    if hasEntities :
        if 'ner' not in nlp.pipe_names:
            print("...adding new pipe 'ner'...")
            ner = nlp.add_pipe('ner')
            restartTraining = True
        else:
            # the pipe 'ner' already exists 
            ner = nlp.get_pipe('ner')
        
    if hasCategories: 
        # 2.a) setup pipeline and categories...
        if 'textcat' not in nlp.pipe_names:
            print("...adding new pipe 'textcat'...")
            textcat = nlp.add_pipe("textcat")
            restartTraining = True
        else:
            # the pipe 'textcat' already exists
            textcat = nlp.get_pipe("textcat")
        
        
    # 3.) add the labels contained in the trainingDataSet...
    for _data in trainingDataSet:
        for ent in _data.entities:
            _label = ent.label
            _labelList = ner.labels
            # We only need to add the label if it is not already part of the entityRecognizer
            if _label not in _labelList:
                print("...adding new entity '" + _label + "'...")
                ner.add_label(_label)

        # add categories
        for cat in _data.categories:
            _label = cat.label
            _labelList = textcat.labels
            # We only need to add the label if it is not already part of the categories
            if _label not in _labelList:
                if restartTraining : 
                    print("...NOT adding new category '" + _label + "'...")
                    #optimizer = nlp.initialize()
                    textcat.add_label(_label)
                else :
                    raise Exception("adding a new category (" + _label + ") to an existing model is not supported by spacy!")

    # Convert the data list to the Spacy Training Data format
    trainingData=datamodel.convertToTrainingData(trainingDataSet)
  
        
    if restartTraining: 
        print("...begin new training!")
        optimizer = nlp.initialize()
    else:
        print("...resume training!")
        optimizer = nlp.resume_training()
    
    
    # new api 3.0  - see: https://spacy.io/usage/v3
    examples = []
    for text, annots in trainingData:
        print("text=", text)
        print("annots=", annots)
        examples.append(Example.from_dict(nlp.make_doc(text), annots))
        
    losses=nlp.update(examples, sgd=optimizer);
    print(losses)
    
    
    # finally we save the updated model to disk
    # print("save model to disk "+modelPath)
    nlp.to_disk(modelPath)
  
    return losses





# Analysing entities for a given text
# The method assumes that a model exists  
def analyseText(analyseData, modelPath):
    nlp = spacy.load(modelPath)  # load existing spaCy model
    
    doc = nlp(analyseData.text)
    print("analyseText started....")
    result = []
    for ent in doc.ents:
        #print(ent.label_, ent.start_char, ent.end_char,ent.text )
        print("    ", ent.label_," = ", ent.text)
        result.append({"label": ent.label_,"text": ent.text})
    return result



