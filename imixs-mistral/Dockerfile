FROM python

RUN pip install llama-cpp-python
RUN pip install fastapi
RUN pip install "uvicorn[standard]"
RUN pip install fastapi-xml

COPY ./app /app
#COPY ./models/mistral*.gguf /app
WORKDIR /app

#CMD ["python", "main.py"]        
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]