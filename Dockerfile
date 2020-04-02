#FROM python:3
FROM tiangolo/uvicorn-gunicorn-fastapi:python3.7

#WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

RUN python -m spacy download en_core_web_sm

COPY . .

#CMD [ "python", "./your-daemon-or-script.py" ]
#CMD [ "python", "./fastapi-test.py" ]
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
