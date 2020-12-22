import sys 
sys.path.append('../..')

from imixs.core import datamodel, datatrain


"""
The goal of this test program is to demonstrate the usage of the training data module

"""


# Startup method
if __name__ == "__main__":
    
    modelfile = "training_model"
    # create training objects
    l=[]
    
   
    
    TRAIN_DATA = [('what is the price of 11,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 12,50', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 10120,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 333,00', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 0,50', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 10,40', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 0,49', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 0,10', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 9,99', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 155,55', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 200,00', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 90120,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 50000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 1600,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 1500,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 70000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 30,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 11,50', {'entities': [(21, 26, 'iban')]})] 
    
    d=datamodel.TrainingData(text='what is the price of 11,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=26) 
    d.entities.append(e)
    l.append(d)
    d=datamodel.TrainingData(text='what is the price of 12,50',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=26) 
    d.entities.append(e)
    l.append(d)
    d=datamodel.TrainingData(text='what is the price of 20000,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=29) 
    d.entities.append(e)
    l.append(d)
    d=datamodel.TrainingData(text='what is the price of 10120,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=29) 
    d.entities.append(e)
    l.append(d)
    d=datamodel.TrainingData(text='what is the price of 333,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=27) 
    d.entities.append(e)
    l.append(d)
    d=datamodel.TrainingData(text='what is the price of 0,50',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=24) 
    d.entities.append(e)
    l.append(d)
    d=datamodel.TrainingData(text='what is the price of 10,40',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=26) 
    d.entities.append(e)
    l.append(d)    
    d=datamodel.TrainingData(text='what is the price of 0,49',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=25) 
    d.entities.append(e)
    l.append(d)        
    d=datamodel.TrainingData(text='what is the price of 0,10',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=25) 
    d.entities.append(e)
    l.append(d)        
    d=datamodel.TrainingData(text='what is the price of 9,99',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=25) 
    d.entities.append(e)
    l.append(d)
    d=datamodel.TrainingData(text='what is the price of 155,55',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=27) 
    d.entities.append(e)
    l.append(d) 
    d=datamodel.TrainingData(text='what is the price of 200,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=27) 
    d.entities.append(e)
    l.append(d) 
    d=datamodel.TrainingData(text='what is the price of 90120,55',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=29) 
    d.entities.append(e)
    l.append(d)     
    d=datamodel.TrainingData(text='what is the price of 50000,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=29) 
    d.entities.append(e)
    l.append(d)     
    d=datamodel.TrainingData(text='what is the price of 1600,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=28) 
    d.entities.append(e)
    l.append(d)        
    d=datamodel.TrainingData(text='what is the price of 1500,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=28) 
    d.entities.append(e)
    l.append(d)            
    d=datamodel.TrainingData(text='what is the price of 70000,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=29) 
    d.entities.append(e)
    l.append(d)   
    d=datamodel.TrainingData(text='what is the price of 30,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=26) 
    d.entities.append(e)
    l.append(d)                
    d=datamodel.TrainingData(text='what is the price of 20,00',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=26) 
    d.entities.append(e)
    l.append(d)            
    d=datamodel.TrainingData(text='what is the price of 11,50',entities=[])
    e=datamodel.Entity(label='iban',start=21,stop=26) 
    d.entities.append(e)
    l.append(d)            
   
    
    # update model
    prdnlp = datatrain.updateModel(l,modelfile)
   
    
    print("------------finished--------------------")
                
                
                
    # Test your text
    test_text = input("Enter your testing text: ")
    doc = prdnlp(test_text)
    for ent in doc.ents:
        print(ent.text, ent.start_char, ent.end_char, ent.label_)
