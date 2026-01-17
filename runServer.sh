#!/bin/bash
cd "$(dirname "$0")"
echo "Starting Server..."
java -cp "out/production/FinalProject:out/production/FinalProject/controller:out/production/FinalProject/model:out/production/FinalProject/server:out/production/FinalProject/storage" server.ServerMain
