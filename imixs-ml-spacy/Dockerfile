FROM tiangolo/uvicorn-gunicorn-fastapi:python3.7

WORKDIR /usr/src/app

COPY requirements.txt ./

RUN pip install -U pip

RUN pip install -U spacy
RUN pip install --no-cache-dir -r requirements.txt
RUN pip list
# download a model
RUN python -m spacy download en_core_web_sm

COPY . .

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
