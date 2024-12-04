#!/usr/bin/env bash
set -e

cd frontend
mvn clean install
./gradlew build
rm -rf build/distributions

cd ../backend
mvn clean install
./gradlew build
rm -rf build/distributions
