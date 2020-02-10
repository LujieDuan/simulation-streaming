FROM python:latest

RUN apt-get update && apt-get install tesseract-ocr -y 

WORKDIR /code

COPY ./requirements.txt /code

RUN pip install -r requirements.txt

COPY . /code

ENTRYPOINT ["python", "influenza-adapter.py"]