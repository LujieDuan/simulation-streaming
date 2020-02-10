FROM python:latest

WORKDIR /code

COPY ./requirements.txt /code

RUN pip install -r requirements.txt

COPY . /code

ENTRYPOINT ["python", "influenza-adapter.py"]