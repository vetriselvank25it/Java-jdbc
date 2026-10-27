@echo off
title KEC R2024 Academic Evaluation System - Interactive Console
cd /d "%~dp0"
echo ==============================================================================
echo        KONGU ENGINEERING COLLEGE (AUTONOMOUS) - REGULATION 2024
echo         Student Academic & Continuous Evaluation System (JDBC)
echo ==============================================================================
echo.
java -cp "bin;lib/mysql-connector-j-8.3.0.jar" com.kec.eval.app.MainApp
pause
