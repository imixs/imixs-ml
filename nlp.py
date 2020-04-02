from fastapi import FastAPI
from pydantic import BaseModel
import spacy



nlp_en = spacy.load("en_core_web_sm")
app = FastAPI()


class Data(BaseModel):
    text: str


@app.post("/text/")
def extract_entities(data: Data, lang: str):
    doc_en = nlp_en(data.text)
    ents = []
    for ent in doc_en.ents:
        ents.append({"text": ent.text, "label_": ent.label_})
    return {"message": data.text, "lang": lang, "ents": ents}