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
    
   
    
    TRAIN_DATA = [('what is the price of 11,00', {'entities': [(21, 26, 'iban')] , 'cats': [('invoice', True)]}), 
              ('what is the price of 12,50', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20000,00', {'entities': [(21, 29, 'iban')] , 'cats': [('biginvoice', True)]}), 
              #('what is the price of 20000,00', {'entities': [(21, 29, 'iban')]}), 
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
    
    
       
    for i in TRAIN_DATA:
        _text=i[0];
        _labels=i[1];
        _entities=_labels.get('entities');
        _cats=_labels.get('cats');
        
        _d=datamodel.TrainingData(text=_text,entities=[],categories=[])
        if _entities is not None:
            for e in _entities:
                _e=datamodel.Entity(label=e[2],start=e[0],stop=e[1])
                _d.entities.append(_e)
        
        if _cats is not None:
            for c in _cats:
                _c=datamodel.Category(label=c[0],included=c[1])
                _d.categories.append(_c)
            
        l.append(_d)
        

    
    # update model
    prdnlp = datatrain.updateModel(l,modelfile)
   
    
    print("------------finished--------------------")
                
                
                
    # Test your text
    test_text = input("Enter your testing text: ")
    doc = prdnlp(test_text)
    for ent in doc.ents:
        print("Entity: ", ent.label_, ent.text, ent.start_char, ent.end_char )

    for cat in doc.cats:
        print("Category: ", cat)



    for label, score in doc.cats.items():
        print(label, score)







