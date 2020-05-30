import sys 
sys.path.append('..')

from pydantic import BaseModel
import spacy
import random
from spacy.util import minibatch, compounding
from imixs.core import datamodel 
"""
The goal of this test program is to demonstrate the usage of the class TrainingData. 

"""


def train(data, iterations):
    
    print("start analyse the data object...")
    # Analyse the data object ......
    for _data in data:
        print("text="+_data.text)
        
        for _entity in _data.entities:
            
            print ("label="+_entity.label)
    
    
    

# Startup method
if __name__ == "__main__":
    
    
    # Create a list of data objects...
    l=[]
     
    e=datamodel.Entity(label='part',start=0,stop=2)
    d=datamodel.TrainingData(text='Some text',entities=[])
    d.entities.append(e)
    d.entities.append(datamodel.Entity(label='orderid',start=0,stop=2))
    l.append(d)
    
    # ....
    prdnlp = train(l, 20)
   
