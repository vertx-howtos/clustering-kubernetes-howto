#/usr/bin/env bash
set -e

cd frontend
mvn clean install
./gradlew build

cd ../backend
mvn clean install
./gradlew build
