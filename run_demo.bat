@echo off
title KEC R2024 Evaluation System - Automated Verification Runner
cd /d "%~dp0"
echo Running automated verification and test demonstration...
echo.
java -cp "bin;lib/mysql-connector-j-8.3.0.jar" com.kec.eval.app.TestSimulation
pause
