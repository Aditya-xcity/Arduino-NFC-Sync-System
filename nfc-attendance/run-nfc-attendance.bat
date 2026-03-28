@echo off
setlocal

echo ========================================
echo NFC Attendance - Full Build and Run
echo ========================================

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

rem === PATHS ===
rem === Update this path if your JAR is not in 'libraryx' ===
set "LIB_JAR=libraryx/jSerialComm-2.11.4.jar"
set "JAVAFX_LIB=C:\Users\Admin\Desktop\PBl_Project\Project\NfcCard_Java\project\openjfx-25.0.2_windows-x64_bin-sdk\javafx-sdk-25.0.2\lib"

rem === CHECK FILES ===
if not exist "%LIB_JAR%" (
echo [ERROR] Missing jSerialComm JAR
pause
exit /b 1
)

if not exist "%JAVAFX_LIB%" (
echo [ERROR] JavaFX not found
pause
exit /b 1
)

echo.
echo [1/3] Cleaning old classes...
rmdir /s /q target\classes 2>nul
mkdir target\classes

echo.
echo [2/3] Compiling ALL Java files (single shot)...

rem === Find all Java source files ===
dir /s /b src\main\java\*.java > sources.txt


javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml ^
	-cp ".;%LIB_JAR%;target\classes" ^
	-d target\classes ^
	@sources.txt


if errorlevel 1 (
	echo.
	echo [ERROR] Compilation failed.
	del sources.txt >nul 2>&1
	pause
	exit /b 1
)

rem === Check if main class file exists ===
if not exist "target\classes\com\nfc\attendance\NFCAttendanceApp.class" (
	echo [ERROR] Main class not found after compilation.
	del sources.txt >nul 2>&1
	pause
	exit /b 1
)


echo.
echo [3/3] Running Application...

java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml ^
	-cp ".;%LIB_JAR%;target\classes" ^
	com.nfc.attendance.NFCAttendanceApp

set "APP_EXIT_CODE=%ERRORLEVEL%"
del sources.txt >nul 2>&1


if not "%APP_EXIT_CODE%"=="0" (
	echo.
	echo [ERROR] Application failed to start.
	exit /b 1
)


echo.
echo [SUCCESS] Application executed successfully!
pause

endlocal

