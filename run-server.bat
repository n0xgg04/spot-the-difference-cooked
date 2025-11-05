@echo off
cd server
call mvn compile dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" com.ltm.game.server.GameServer
pause
