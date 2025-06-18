FROM alpine:latest
LABEL authors="adampoi"

FROM gradle:jdk21-alpine

WORKDIR /workspace

# Copy gradle files first for better caching
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle/ gradle/

# Download dependencies
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src/ src/

# Run tests
CMD ["gradle", "test", "--no-daemon", "--info"]