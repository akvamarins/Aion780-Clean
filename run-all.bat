@echo off
set "ROOT=%~dp0"
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
set "JAVA=%JAVA_HOME%\bin\java.exe"
set "DIST=%ROOT%build\dist"

echo Checking libs...
dir "%DIST%\AL-Login\lib" | find "logback"
if errorlevel 1 (
  echo !!! lib пустая - запускаю build.bat для копирования либ !!!
  call "%ROOT%build.bat"
)

echo === Starting AL-Login with classpath ===
start "AL-Login" /D "%DIST%\AL-Login" "%JAVA%" -Xms256m -Xmx512m -cp "AL-Login-8.0-SNAPSHOT.jar;lib/*;../AL-Commons/target/*;../../AL-Commons/target/classes" com.aionemu.loginserver.LoginServer

timeout /t 5 /nobreak >nul

echo === Starting AL-Game ===
start "AL-Game" /D "%DIST%\AL-Game" "%JAVA%" -Xms1024m -Xmx2048m -cp "AL-Game-8.0-SNAPSHOT.jar;lib/*;../AL-Commons/target/*;../../AL-Commons/target/classes" com.aionemu.gameserver.GameServer

echo === Starting AL-Chat ===
start "AL-Chat" /D "%DIST%\AL-Chat" "%JAVA%" -Xms128m -Xmx256m -cp "AL-Chat-8.0-SNAPSHOT.jar;lib/*" com.aionemu.chatserver.ChatServer

pause