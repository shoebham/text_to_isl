FROM python:3.9

COPY --from=openjdk:8-jre-slim /usr/local/openjdk-8 /usr/local/openjdk-8
ENV JAVA_HOME /usr/local/openjdk-8
RUN update-alternatives --install /usr/bin/java java /usr/local/openjdk-8/bin/java 1

# Set working directory
WORKDIR /root

# Copy requirements.txt into the container
COPY requirements.txt .
COPY . .
# Install dependencies from requirements.txt

RUN pip install --upgrade pip setuptools wheel
RUN pip install spacy
RUN pip install -r requirements.txt

# Copy the stanza_resources directory into the container
COPY stanza_resources /root/stanza_resources

# Add your main.py file into the container
ADD main.py .

# Expose port 5000
EXPOSE 5000

# Run your Flask application using flask run --host=0.0.0.0
CMD ["python", "main.py"]
