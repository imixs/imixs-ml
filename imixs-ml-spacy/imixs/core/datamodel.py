from typing import List
from pydantic import BaseModel

"""
The datamodel defines the Data class used for training data
A data object consists of a text and a list of entity definitions. 

The data object is needed to support the data structure for the Rest API based on the pydantic BaseModel 


@author: ralph.soika@imixs.com
@version:  1.0
"""


# An Entity class defines a Entity object with its name and its position in the data objects text.
class Entity(BaseModel):
    label: str
    start: int
    stop: int
    

# The Data class defines an object containing a text and a list of entities within the text
class TrainingData(BaseModel):
    text: str 
    entities: List[Entity] 



# The Data class defines an object containing a text to be analsed
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
        for _entity in _data.entities:
            # append a new tuple for each entity....
            entList.append((_entity.start, _entity.stop, _entity.label))            
        # create a dictionary with the entity list. 
        dict = {"entities" : entList, }
        result.append((_data.text, dict))
    # now we have the traing data structure
    return result
    
