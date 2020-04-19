import spacy
nlp = spacy.load('en')
doc = nlp('Hello     World!')
for token in doc:
    print('"' + token.text + '"')
 
# "Hello"
# "    "
# "World"
# "!"