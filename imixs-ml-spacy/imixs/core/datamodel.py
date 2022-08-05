"""
The trainingmodel defines the Data classes used for training data and analysing text.
A TrainingData object consists of a text and a list of entity definitions and categories. 
The data object is needed to support the data structure for the Rest API based on the pydantic BaseModel 


@author: ralph.soika@imixs.com 
@version:  2.0 
""" 
# An Entity class defines a Entity object with its name and its position in the data objects text.

from pydantic import BaseModel
from typing import List
import json


class TrainingEntity(BaseModel):
    label: str
    start: int
    stop: int
    
# An Category class defines a category object with its name and a boolean flag indicating if the category is enclosed or not
class TrainingCategory(BaseModel):
    label: str
    enclosed: bool

# The Data class defines an object containing a text and a list of entities within the text
class TrainingData(BaseModel):
    text: str 
    entities: List[TrainingEntity] 
    categories: List[TrainingCategory] 





# The AnalyseData class defines an object containing a text to be analysed
class AnalyseData(BaseModel):
    text: str 

    

# This helper method can be used to convert an array of Data objects into
# the typically SpaCy training data structure:
#
#   [('what is the price of polo?', {'entities': [(21, 25, 'iban')]}),
#    ('what is the price of ball?', {'entities': [(21, 25, 'po-number')]}),
#    ('what is the price of jegging?', {'entities': [(21, 28, 'iban')]})
#   ]
#     
def convertToTrainingData(dataList):
    result = []
    
    for _data in dataList:
        entList = []
        catList = []
        for _entity in _data.entities:
            # append a new tuple for each entity....
            entList.append((_entity.start, _entity.stop, _entity.label))            

        for _category in _data.categories:
            # append a new tuple for each category....
            catList.append((_category.label, _category.enclosed))            

        
        # create a dictionary with the entity list. 
        #dict = {"entities" : entList, }
        dict_tmp = {"entities" : entList, "cats" : catList }
        result.append((_data.text, dict_tmp))
    # now we have the traing data structure
    return result
    
# This helper method reads a json file into a training structure
# The method build an array of TraingData objects
#
def readTrainingDataFromJSONFile(datafile):
    training_data=[]
    # Opening JSON file
    with open(datafile, 'r') as JSON:
       json_dict = json.load(JSON)
     
    for i in json_dict:
        _text=i['text'];
        _entities=i['entities'];        
        _d=TrainingData(text=_text,entities=[],categories=[])
        if _entities is not None:
            for e in _entities:
                _e=TrainingEntity(label=e['label'],start=e['start'],stop=e['stop'])
                _d.entities.append(_e)        
        training_data.append(_d)
    return training_data
