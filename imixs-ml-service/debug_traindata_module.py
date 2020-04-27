import datamodel 
import datatrain


"""
The goal of this test program is to demonstrate the usage of the training data module

"""


# Startup method
if __name__ == "__main__":
    
    modelfile = "training_model"
    # create training objects
    l=[]
    
    """
    d=datamodel.TrainingData(text='Please transfer the invoice amount of EUR 100,00 ',entities=[])
    e=datamodel.Entity(label='part',start=0,stop=6) # 36 47
    d.entities.append(e)
    l.append(d)
    
    d=datamodel.TrainingData(text='Please pay the invoice amount of 1,99 EUR. ',entities=[])
    e=datamodel.Entity(label='part',start=0,stop=6) # 33 36
    d.entities.append(e)
    l.append(d)


    d=datamodel.TrainingData(text='We ask you to pay the invoice for 3000,00 EUR ',entities=[])
    e=datamodel.Entity(label='part',start=0,stop=2)  # 34 40
    d.entities.append(e)
    l.append(d)
    """
    
    d=datamodel.TrainingData(text='what is the price of 0,49',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=24) 
    d.entities.append(e)
    l.append(d)
    
    d=datamodel.TrainingData(text='what is the price of 155,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 155,00',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 333,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 445,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 665,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 155,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 755,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 885,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)


    d=datamodel.TrainingData(text='what is the price of 50000,00',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=28)  # 34 40
    d.entities.append(e)
    l.append(d)
    
    
    
    
    d=datamodel.TrainingData(text='what is the price of 1,49',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=24) 
    d.entities.append(e)
    l.append(d)
    
    d=datamodel.TrainingData(text='what is the price of 156,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 157,00',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 334,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 444,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 662,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 151,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 750,55',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)

    d=datamodel.TrainingData(text='what is the price of 880,05',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=26) # 33 36
    d.entities.append(e)
    l.append(d)


    d=datamodel.TrainingData(text='what is the price of 50000,00',entities=[])
    e=datamodel.Entity(label='part',start=21,stop=28)  # 34 40
    d.entities.append(e)
    l.append(d)
    
    
    # update model
    prdnlp = datatrain.updateModel(l, 20,modelfile)
   
    
    print("------------finished--------------------")
                
                
                
    # Test your text
    test_text = input("Enter your testing text: ")
    doc = prdnlp(test_text)
    for ent in doc.ents:
        print(ent.text, ent.start_char, ent.end_char, ent.label_)
