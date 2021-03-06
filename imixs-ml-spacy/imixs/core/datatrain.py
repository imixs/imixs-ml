import spacy
import random
import time
import os

from spacy.util import minibatch, compounding
from imixs.core import datamodel

"""
This module provides methods to train a model or to analyse text on an 
existing model. 


@author: ralph.soika@imixs.com
@version:  2.0
"""





#
# This method expects a single training data set and updates the given model.
# If no model exists the method will create a new one
#     
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
             
        
        print("<<=========== TEXT BEGIN ===========>>")
        print(_data.text)
        print("<<=========== TEXT END   ===========>>")
        for entity in _data.entities:
            ausschnitt=_data.text[entity.start:entity.stop]
            print("entities  ==>>  "+entity.label + "=" + ausschnitt + " ("+ str(entity.start) + "," + str(entity.stop) + ")")
        
    
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
        # print("Created blank '" +language + "' model")
    
    
    # 2.) set up the pipeline and entity recognizer.
    if hasEntities :
        if 'ner' not in nlp.pipe_names:
            # print("we have no ner so we create an empty one...")
            ner = nlp.create_pipe('ner')
            nlp.add_pipe(ner)
            restartTraining = True
        else:
            # print("we have a ner so we fetch it...")
            ner = nlp.get_pipe('ner')
        
    if hasCategories: 
        # 2.a) setup pipeline and categories...
        if 'textcat' not in nlp.pipe_names:
            #textcat = nlp.create_pipe("textcat", config={"exclusive_classes": False})
            textcat = nlp.create_pipe("textcat")
            nlp.add_pipe(textcat) 
            restartTraining = True
        else:
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
                print("...adding new category '" + _label + "'...")
                textcat.add_label(_label)

      
    # get names of other pipes to disable them during training
    #pipe_exceptions = ["ner", "textcat", "trf_wordpiecer", "trf_tok2vec"]
    #pipe_exceptions = ["trf_wordpiecer", "trf_tok2vec"]
    pipe_exceptions = []
    if (hasEntities): 
        pipe_exceptions.append("ner")
    if (hasCategories): 
        pipe_exceptions.append("textcat")
    
    pipe_exceptions.append("trf_wordpiecer")
    pipe_exceptions.append("trf_tok2vec")
    
    other_pipes = [pipe for pipe in nlp.pipe_names if pipe not in pipe_exceptions]
        
    
    lMilis = int(round(time.time() * 1000))
    with nlp.disable_pipes(*other_pipes):  # only train NER
        # reset and initialize the weights randomly – but only if we're
        # training a new model
        #if not modelExists:
        
        # CHANGE - We always begin a new training because input categories can change...
        if restartTraining: 
            print("begin new training!")
            nlp.begin_training()
        #nlp.begin_training()
        
        # Convert the data list to the Spacy Training Data format
        trainingData=datamodel.convertToTrainingData(trainingDataSet)
        
        losses = {}
        for text, annotations in trainingData:
            #nlp.update([text], [annotations], drop=0.5,losses=losses)
            nlp.update([text], [annotations],losses=losses)
            print("Losses", losses)
        
      
    # print("total time = "+str(int(round(time.time() * 1000))-lMilis) + "ms  ")        
  
    # finally we save the updated model to disk
    # print("save model to disk "+modelPath)
    nlp.to_disk(modelPath)
  
    return nlp





#
# This is a testing method. The method expects a training data set and a number of interations. 
# The method applys the training set mulitple times. The method is only used for 
# developer tests. It should never be used for production..
# The method updates the given model.
# If no model exists the method will create a new one
#     
def updateModelWithInteration(trainingDataSet, iterations, modelPath):
    
    print("!!!!! the method updateModelWithInteration should be used for junit tests only !!!!!")
    
    print("starting updateModel....")
    

    # Read language
    language=os.getenv('MODEL_LANGUAGE', 'en')
    
    # Analyse the data object ......
    for _data in trainingDataSet:
        print("<<=========== TEXT BEGIN ===========>>")
        print(_data.text)
        print("<<=========== TEXT END   ===========>>")
        for entity in _data.entities:
            ausschnitt=_data.text[entity.start:entity.stop]
            print("entities  ==>>  "+entity.label + "=" + ausschnitt + " ("+ str(entity.start) + "," + str(entity.stop) + ")")
        
        
    
    # Test if the model exists
    modelExists=os.path.isdir(modelPath)
    
     # 1.) load model or create blank Language class
    """Load the model, set up the pipeline and train the entity recognizer."""
    if modelExists:
        nlp = spacy.load(modelPath)  # load existing spaCy model
        print("Loaded model '%s'" % modelPath)
    else:
        nlp = spacy.blank(language)  # create blank Language class
        print("Created blank '" +language + "' model")
    
    
  
    
    # 2.) set up the pipeline and entity recognizer.
    if 'ner' not in nlp.pipe_names:
        # print("we have no ner so we create an empty one...")
        ner = nlp.create_pipe('ner')
        nlp.add_pipe(ner)
    else:
        # print("we have a ner so we fetch it...")
        ner = nlp.get_pipe('ner')
        
        
        
    # 3.) add the labels contained in the trainingDataSet...
    for _data in trainingDataSet:
        for ent in _data.entities:
            _label = ent.label
            _labelList = ner.labels
            # We only need to add the label if it is not already part of the entityRecognizer
            if _label not in _labelList:
                # print("...adding new label '" + _label + "'...")
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
                    #drop=0.5,  # dropout - make it harder to memorise data
                    losses=losses,
                )            
            
            print("Losses", losses)
            
    print("total time = "+str(int(round(time.time() * 1000))-lMilis) + "ms  ")        
  
    # finally we save the updated model to disk
    # print("save model to disk "+modelPath)
    nlp.to_disk(modelPath)
  
    return nlp





# Analysing entities for a given text
# The method assumes that a model exists  
def analyseText(analyseData, modelPath):
    nlp = spacy.load(modelPath)  # load existing spaCy model
    
    doc = nlp(analyseData.text)
    print("===============> analyse Text....")
    result = []
    for ent in doc.ents:
        #print(ent.label_, ent.start_char, ent.end_char,ent.text )
        print("    ", ent.label_," = ", ent.text)
        result.append({"label": ent.label_,"text": ent.text})
        
    print("===============")
    print(" ")
    print(" ")
    return result



