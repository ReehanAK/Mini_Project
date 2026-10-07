@echo off
title FoodieHub — Online Food Ordering System
color 0B
echo ====================================================================
echo   FOODIEHUB — ONLINE FOOD ORDERING SYSTEM (Swing + MySQL)
echo ====================================================================
echo.

REM 1. Check if bin directory exists
if not exist "bin" mkdir "bin"

REM 2. Compile Java sources
echo [1/2] Compiling Java source files...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please check if JDK 17+ is installed.
    pause
    exit /b %ERRORLEVEL%
)

echo [OK] Compilation successful.
echo.

REM 3. Launch the Swing GUI
echo [2/2] Launching GUI Application...
echo (Connecting to MySQL on 127.0.0.1:3306...)
echo.
java -cp "bin;lib/*" com.foodsystem.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error.
    pause
)
