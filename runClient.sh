#!/bin/bash
cd "$(dirname "$0")"
java -cp "out/production/FinalProject:out/production/FinalProject/controller:out/production/FinalProject/model:out/production/FinalProject/gui:out/production/FinalProject/storage" gui.LoginWindow
