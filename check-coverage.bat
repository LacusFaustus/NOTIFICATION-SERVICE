@echo off
echo Checking test coverage...
mvn clean test jacoco:report
echo.
echo Coverage report generated at: target/site/jacoco/index.html
echo Opening coverage report...
start target/site/jacoco/index.html
