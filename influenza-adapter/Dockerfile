FROM python:latest

RUN apt update && apt install tesseract

WORKDIR /code

COPY ./requirements.txt /code

RUN pip install -r requirements.txt

COPY . /code

ENTRYPOINT ["python", "influenza-adapter.py"]