@echo off
cd /d "%~dp0"
echo ========================================
echo NFC Attendance - Running with Maven
echo ========================================
echo.

echo [1/2] Cleaning and compiling...
call mvn clean compile

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Running application...
call mvn javafx:run

if %errorlevel% neq 0 (
    echo [ERROR] Application failed to start!
    pause
    exit /b 1
)

pause