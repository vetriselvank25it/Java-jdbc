@echo off
title Compile KEC R2024 System
cd /d "%~dp0"
if not exist "bin" mkdir bin
echo Compiling Java source files...
javac -d bin -cp "bin;lib/mysql-connector-j-8.3.0.jar" src\com\kec\eval\model\*.java src\com\kec\eval\db\*.java src\com\kec\eval\engine\*.java src\com\kec\eval\dao\*.java src\com\kec\eval\app\*.java
if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Compilation completed successfully!
) else (
    echo.
    echo [ERROR] Compilation failed.
)
pause
