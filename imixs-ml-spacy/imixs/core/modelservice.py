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
    

import os
import spacy
from typing import List
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
        #print("text=", text)
        print("annots=", annots)
        examples.append(Example.from_dict(nlp.make_doc(text), annots))
        
    losses=nlp.update(examples, sgd=optimizer);
    print(losses)
    
    # finally we save the updated model to disk
    # print("save model to disk "+modelPath)
    nlp.to_disk(modelPath)
  
    return losses




"""
 Analysing entities for a given text
 The method assumes that a model exists  
"""
def analyseText(analyseData, modelPath):

    print("analyseText started....")
    modelExists=os.path.isdir(modelPath)
    if not modelExists :
        print("model '" + modelPath  + "' not found!")
        raise Exception("model '" + modelPath  + "' not found!")
    
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
        print("    entity: ", ent.label_," = ", ent.text)
        result['entities'].append({"label": ent.label_,"text": ent.text})

    # add all categories
    for label, score in doc.cats.items():
        print("  category: ", label, " score=" + str(score))
        result['categories'].append({"category": label,"score": str(score)})

    return result


"""
 This is a helper method to initialize a blank model by a given
 set of categories. 
 See: https://github.com/imixs/imixs-ml/issues/46
"""
def initModelByCategories (categories: List[str], modelPath):
    
    print("init new model ' " + modelPath + "'...")
    # Read language
    language=os.getenv('MODEL_LANGUAGE', 'en')
    # Test if the model exists
    modelExists=os.path.isdir(modelPath)
    if modelExists:
        raise Exception("model '" + modelPath  + "' already exists!")
    else:
        nlp = spacy.blank(language)  # create blank Language class
        
        if 'ner' not in nlp.pipe_names:
            print("...adding new pipe 'ner'...")
            nlp.add_pipe('ner')

        
        if categories:
            if 'textcat' not in nlp.pipe_names:
                print("...adding new pipe 'textcat'...")
                textcat = nlp.add_pipe("textcat")
            # add categories
            for _cat in categories:
                _labelList = textcat.labels
                # We only need to add the label if it is not already part of the categories
                if _cat not in _labelList:
                        print("...NOT adding new category '" + _cat + "'...")
                        textcat.add_label(_cat)        

    nlp.initialize()
    print ("Model initalized!")
    nlp.to_disk(modelPath)
    return categories
    

   