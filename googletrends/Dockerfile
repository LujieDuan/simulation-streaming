FROM node:12.4.0

RUN apt-get update && apt-get install tesseract-ocr -y 

WORKDIR /code

COPY ./nodecollector/package.json /code

RUN npm install

COPY ./nodecollector/ /code
