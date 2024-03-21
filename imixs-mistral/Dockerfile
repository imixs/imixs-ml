FROM python

RUN pip install llama-cpp-python

COPY ./app /app
COPY ./models/mistral*.gguf /app
WORKDIR /app

CMD ["python", "test.py"]         // equivalent to python rng.py