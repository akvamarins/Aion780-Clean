@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
set "JAVA=%JAVA_HOME%\bin\java.exe"
cd /d "%~dp0build\dist\AL-Login"
"%JAVA%" -Xms256m -Xmx512m -cp "AL-Login-8.0-SNAPSHOT.jar;lib/*" com.aionemu.loginserver.LoginServer
pause