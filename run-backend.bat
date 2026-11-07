@echo off
title Coffee & More - Java Backend & MySQL Server
cls
echo ================================================================================
echo   COFFEE & MORE - KAMARAJ DOMESTIC AIRPORT CHENNAI
echo   Starting Java Backend Server (Port 8080) with MySQL Integration...
echo ================================================================================
echo.

cd /d "%~dp0"

echo [1/2] Compiling Java source files...
if not exist "backend\bin" mkdir "backend\bin"
javac -encoding UTF-8 -cp "backend\lib\*" -d "backend\bin" backend\src\com\coffeeandmore\*.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ is installed.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Java Server...
echo.
java -cp "backend\bin;backend\lib\*" com.coffeeandmore.CoffeeAndMoreServer

pause

