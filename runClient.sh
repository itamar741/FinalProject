#!/bin/bash
cd "$(dirname "$0")"
java -cp "out/production/FinalProject:out/production/FinalProject/controller:out/production/FinalProject/model:out/production/FinalProject/gui:out/production/FinalProject/storage:libs/poi-5.2.5.jar:libs/poi-ooxml-5.2.5.jar:libs/xmlbeans-5.2.0.jar:libs/commons-compress-1.24.0.jar:libs/commons-codec-1.16.0.jar" gui.LoginWindow
