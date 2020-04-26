from pydantic import BaseModel
import spacy
import random
from spacy.util import minibatch, compounding
import datamodel 
"""
The goal of this test program is to demonstrate the usage of the data class. 

In addition we convert the data object into the typical  Spacy Training Data format

"""


TRAIN_DATA_EXAMPLE = [('what is the price of polo?', {'entities': [(21, 25, 'iban')]}),
              ('what is the price of ball?', {'entities': [(21, 25, 'po-number')]})]

def train(data):
    
    print("start analyze the data object...")
    # Analyze the data object ......
    for _data in data:
        print("text="+_data.text)
        for _entity in _data.entities:
            print ("label="+_entity.label)
    
    test=datamodel.convertToTrainingData(data)
    print("finished")
   
    



# Startup method
if __name__ == "__main__":    
    
    # Create a list of data objects...
    dataList=[]
     
    e=datamodel.Entity(label='part',start=0,stop=2)
    d=datamodel.Data(text='what is the price of polo?',entities=[])
    d.entities.append(e)
    d.entities.append(datamodel.Entity(label='orderid',start=0,stop=2))
    dataList.append(d)
    
    e=datamodel.Entity(label='part',start=0,stop=2)
    d=datamodel.Data(text='what is the price of ball?',entities=[])
    d.entities.append(e)
    d.entities.append(datamodel.Entity(label='orderid',start=0,stop=2))
    dataList.append(d)
    
    # ....
    prdnlp = train(dataList)
   
