from typing import List
from pydantic import BaseModel

"""
The datamodel defines the Data class used for training data
A data object consists of a text and a list of entity definitions and categories. 

The data object is needed to support the data structure for the Rest API based on the pydantic BaseModel 


@author: ralph.soika@imixs.com
@version:  2.0
"""


# An Entity class defines a Entity object with its name and its position in the data objects text.
class Entity(BaseModel):
    label: str
    start: int
    stop: int
    
# An Category class defines a category object with its name and a boolean flag indicating if the category is included or excluded
class Category(BaseModel):
    label: str
    included: bool

# The Data class defines an object containing a text and a list of entities within the text
class TrainingData(BaseModel):
    text: str 
    entities: List[Entity] 
    categories: List[Category] 



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
        catList = []
        for _entity in _data.entities:
            # append a new tuple for each entity....
            entList.append((_entity.start, _entity.stop, _entity.label))            

        for _category in _data.categories:
            # append a new tuple for each category....
            catList.append((_category.label, _category.included))            

        
        # create a dictionary with the entity list. 
        #dict = {"entities" : entList, }
        dict = {"entities" : entList, "cats" : catList }
        result.append((_data.text, dict))
    # now we have the traing data structure
    return result
    
