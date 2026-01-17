@echo off
cd /d "%~dp0"
echo Starting Server...
java -cp "out/production/FinalProject;out/production/FinalProject/controller;out/production/FinalProject/model;out/production/FinalProject/server;out/production/FinalProject/storage" server.ServerMain
pause
