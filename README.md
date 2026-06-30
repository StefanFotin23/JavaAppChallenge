Deploy the app:
    docker-compose up --build -d

Unit tests:
    mvn clean test

Formatting and checkstyle:
    mvn spotless:apply