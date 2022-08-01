"""
This module provides methods to train a model or to analyse text on an 
existing model. 

The method 'updateModel' expects a training datamodel. The implementation is based on 
the concept of 'incremental training'. Typically the method expects only one training document,
but also a list of training objects will be processed.

The method 'analyseText' is used get the results of a trained model based on a given text.


@see https://spacy.io/api/language#update
@author: ralph.soika@imixs.com
@version:  2.0
"""

"""
 This method 'updateModel' expects a training data set containing one or many training objects.
 The method updates the given model. If no model exists the method will create a new one.
 
 The method did not use the minibatch algorithm provided by spacy because the assumption is that 
 only one document is trained in a 'incremental training mode' on each call.
 
""" 

import os

import spacy
from thinc.api import Config

from imixs.core import datamodel
from spacy.pipeline.textcat_multilabel import multi_label_bow_config, multi_label_cnn_config
from spacy.training import Example
from typing import List
import logging

def updateModel(trainingDataSet, modelPath, min_losses):

    logging.info("...updateModel....")
    
    # Read language
    language = os.getenv('MODEL_LANGUAGE', 'xx')
    
    # Analyse the data object ......
    hasEntities = False
    hasCategories = False
    restartTraining = False
    
    for _data in trainingDataSet:
        if (len(_data.entities) > 0):
            hasEntities = True
        if (len(_data.categories) > 0):
            hasCategories = True
    
    # Test if the model exists
    modelExists = os.path.isdir(modelPath)
    
    # 1.) load model or create blank Language class 
    """Load the model, set up the pipeline and train the entity recognizer."""
    if modelExists:
        nlp = spacy.load(modelPath)  # load existing spaCy model
        # print("Loaded model '%s'" % modelPath)
    else:
        # nlp = spacy.blank(language)  # create blank Language class
        nlp = initModel(modelPath)
        restartTraining = True
        logging.info("...creating blank model, language='" + language + "'")
    
    # 2.) set up the pipeline and entity recognizer.
    if hasEntities:
        if 'ner' not in nlp.pipe_names:
            logging.info("...adding new pipe 'ner'...")
            ner = nlp.add_pipe('ner')
            nlp.add_pipe()
            restartTraining = True
        else:
            # the pipe 'ner' already exists 
            ner = nlp.get_pipe('ner')
        
    if hasCategories: 
        # 2.a) setup pipeline and categories...
        if 'textcat_multilabel' not in nlp.pipe_names:
            logging.info("...adding new pipe 'textcat_multilabel'...")
            # textcat = nlp.add_pipe("textcat_multilabel")
            
            # Now we need to manually config the multilabel feature
            textcat = nlp.add_pipe("textcat_multilabel", config=Config().from_str(multi_label_bow_config))
            #textcat = nlp.add_pipe("textcat_multilabel", config=Config().from_str(multi_label_cnn_config))
            assert nlp.get_pipe("textcat_multilabel").is_resizable
            # See duscussion here https://github.com/explosion/spaCy/discussions/6905
            # maybe an alternative is:
            # nlp.add_pipe("textcat_multilabel", config=Config().from_str(multi_label_cnn_config))
            restartTraining = True
        else:
            # the pipe 'textcat' already exists
            textcat = nlp.get_pipe("textcat_multilabel")
        
    # 3.) add the labels contained in the trainingDataSet...
    for _data in trainingDataSet:
        for ent in _data.entities:
            _label = ent.label
            _labelList = ner.labels
            # We only need to add the label if it is not already part of the entityRecognizer
            if _label not in _labelList:
                logging.info("...adding new entity '" + _label + "'...")
                ner.add_label(_label)

        # add categories
        for cat in _data.categories:
            _label = cat.label
            _labelList = textcat.labels
            # We only need to add the label if it is not already part of the categories
            if _label not in _labelList:
                logging.info("...adding new category '" + _label + "'...")
                # textcat.initialize(lambda: [], nlp=nlp)
                # optimizer = nlp.initialize()
                textcat.add_label(_label)               
                restartTraining = True
                
                # if restartTraining : 
                #    print("...NOT adding new category '" + _label + "'...")
                #    
                # else :
                #    raise Exception("adding a new category (" + _label + ") to an existing model is not supported by spacy!")

    # Convert the data list to the Spacy Training Data format
    trainingData = datamodel.convertToTrainingData(trainingDataSet)
        
    if restartTraining: 
        logging.info("...begin new training!")
        optimizer = nlp.initialize()
    else:
        logging.info("...resume training!")
        optimizer = nlp.resume_training()
    
    # new api 3.0  - see: https://spacy.io/usage/v3
    examples = []
    for text, annots in trainingData:
        logging.info("---- TEXT START ----------------------")
        logging.info(text)
        logging.info("---- TEXT END   ----------------------")
        logging.info("---- ANNOTS: "+ str(annots))
        examples.append(Example.from_dict(nlp.make_doc(text), annots))
        
    losses = nlp.update(examples, sgd=optimizer);
    logging.info("---- RESULT: losses="+str(losses))
    
    # finally we save the updated model to disk
    writeToDisk=True
    if ("ner" in losses):
        if losses.get('ner')<min_losses:
            writeToDisk=False
    
    if (writeToDisk == True):
        # We update the model only if the losses is  > then a given min_losses 
        logging.info("writing new model to disk - min_losses= "+str(min_losses))
        nlp.to_disk(modelPath)
    else: 
        logging.info("no model update - min_losses= "+str(min_losses))
  
    return losses

"""
 Analysing entities for a given text
 The method assumes that a model exists  
"""


def analyseText(analyseData, modelPath):

    logging.info("analyseText started....")
    modelExists = os.path.isdir(modelPath)
    if not modelExists:
        logging.info("model '" + modelPath + "' not found!")
        raise Exception("model '" + modelPath + "' not found!")
    
    nlp = spacy.load(modelPath)  # load existing spaCy model    
    # print(analyseData.text)
    doc = nlp(analyseData.text)
       
    # build response data structure based on a dictionary
    # Example: 
    # {"entities":[{"label":"person","text":"Wayne Rooney"}],"categories":[{"label":"sports","score":0.999}]}
    result = {};
    result['entities'] = []
    result['categories'] = []
    # add all entities
    for ent in doc.ents:
        logging.info("    entity: " + str(ent.label_) +  " = " + str(ent.text))
        result['entities'].append({"label": ent.label_, "text": ent.text})

    # add all categories
    for label, score in doc.cats.items():
        logging.info("  category: " + str(label) + " score=" + str(score))
        result['categories'].append({"category": label, "score": str(score)})

    return result


"""
 This is a helper method to initialize a blank model by a given
 model path. 
 The method did not create any pipelines as this is managed
 by the 'updateModel' method depending on the given traing data.
"""
def initModel (modelPath):
    
    logging.info("...init new model ' " + modelPath + "'...")
    # Read language
    language = os.getenv('MODEL_LANGUAGE', 'xx')
    # Test if the model exists
    modelExists = os.path.isdir(modelPath)
    if modelExists:
        raise Exception("model '" + modelPath + "' already exists!")
    
    nlp = spacy.blank(language)  # create blank Language class
    # NOTE: the pipelines will be added during the updateModel method.     
   
    # nlp.initialize()
    logging.info ("...new Model created successful.")
    nlp.to_disk(modelPath)
    return nlp
   
