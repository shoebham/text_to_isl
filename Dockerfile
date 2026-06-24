FROM python:3.9

COPY --from=openjdk:8-jre-slim /usr/local/openjdk-8 /usr/local/openjdk-8
ENV JAVA_HOME=/usr/local/openjdk-8
RUN update-alternatives --install /usr/bin/java java /usr/local/openjdk-8/bin/java 1

# Set working directory
WORKDIR /root

# Install dependencies first (better layer caching)
COPY requirements.txt .
RUN pip install --upgrade pip setuptools wheel \
    && pip install "spacy>=3.7,<3.8" \
    && pip install -r requirements.txt \
    && python -m spacy download en_core_web_sm || true

# Copy application source (stanza_resources is optional; main.py downloads if missing)
COPY . .

# Expose port 5000
EXPOSE 5000

# Run your Flask application
CMD ["python", "main.py"]
