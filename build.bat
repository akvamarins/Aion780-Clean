@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
set "MVN=C:\Users\viktors\maven\bin\mvn.cmd"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d %~dp0

echo === 1. Cleaning ===
rmdir /s /q build 2>nul

echo === 2. Maven Build ===
call %MVN% clean install -DskipTests
if errorlevel 1 (
  echo BUILD FAILED
  pause
  exit /b
)

echo === 2b. Copy dependencies - ЭТО ФИКСИТ lib ===
call %MVN% dependency:copy-dependencies -DoutputDirectory=%cd%\AL-Login\target\lib -pl AL-Login -am -DskipTests -DincludeScope=runtime
call %MVN% dependency:copy-dependencies -DoutputDirectory=%cd%\AL-Game\target\lib -pl AL-Game -am -DskipTests -DincludeScope=runtime
call %MVN% dependency:copy-dependencies -DoutputDirectory=%cd%\AL-Chat\target\lib -pl AL-Chat -am -DskipTests -DincludeScope=runtime

echo === 3. Creating build/dist ===
mkdir build\dist\AL-Game\lib 2>nul
mkdir build\dist\AL-Login\lib 2>nul
mkdir build\dist\AL-Chat\lib 2>nul
mkdir build\dist\AL-Game\config 2>nul
mkdir build\dist\AL-Login\config 2>nul
mkdir build\dist\AL-Chat\config 2>nul

copy AL-Game\target\AL-Game-8.0-SNAPSHOT.jar build\dist\AL-Game\ /Y
copy AL-Login\target\AL-Login-8.0-SNAPSHOT.jar build\dist\AL-Login\ /Y
copy AL-Chat\target\AL-Chat-8.0-SNAPSHOT.jar build\dist\AL-Chat\ /Y
copy AL-Commons\target\AL-Commons-8.0-SNAPSHOT.jar build\dist\AL-Game\lib\ /Y
copy AL-Commons\target\AL-Commons-8.0-SNAPSHOT.jar build\dist\AL-Login\lib\ /Y
copy AL-Commons\target\AL-Commons-8.0-SNAPSHOT.jar build\dist\AL-Chat\lib\ /Y

xcopy /s /y /i AL-Game\target\lib\* build\dist\AL-Game\lib\
xcopy /s /y /i AL-Login\target\lib\* build\dist\AL-Login\lib\
xcopy /s /y /i AL-Chat\target\lib\* build\dist\AL-Chat\lib\

REM Копируем config/data если есть
if exist AL-Game\config xcopy /s /y /i AL-Game\config\* build\dist\AL-Game\config\
if exist AL-Game\data xcopy /s /y /i AL-Game\data\* build\dist\AL-Game\data\
if exist AL-Game\sql xcopy /s /y /i AL-Game\sql\* build\dist\AL-Game\sql\
if exist AL-Login\config xcopy /s /y /i AL-Login\config\* build\dist\AL-Login\config\
if exist AL-Login\data xcopy /s /y /i AL-Login\data\* build\dist\AL-Login\data\
if exist AL-Login\sql xcopy /s /y /i AL-Login\sql\* build\dist\AL-Login\sql\
if exist AL-Chat\config xcopy /s /y /i AL-Chat\config\* build\dist\AL-Chat\config\

REM Если config в корне (как в старых сборках)
if exist config xcopy /s /y /i config\* build\dist\AL-Game\config\ 2>nul
if exist data xcopy /s /y /i data\* build\dist\AL-Game\data\ 2>nul

echo @echo off> build\dist\start-login.bat
echo cd /d %%~dp0\AL-Login>> build\dist\start-login.bat
echo "%JAVA_HOME%\bin\java" -Xms256m -Xmx512m -cp "AL-Login-8.0-SNAPSHOT.jar;lib/*" com.aionemu.loginserver.LoginServer>> build\dist\start-login.bat
echo pause>> build\dist\start-login.bat

echo @echo off> build\dist\start-game.bat
echo cd /d %%~dp0\AL-Game>> build\dist\start-game.bat
echo "%JAVA_HOME%\bin\java" -Xms1024m -Xmx2048m -XX:+UseG1GC -cp "AL-Game-8.0-SNAPSHOT.jar;lib/*" com.aionemu.gameserver.GameServer>> build\dist\start-game.bat
echo pause>> build\dist\start-game.bat

echo @echo off> build\dist\start-all.bat
echo start "Login" cmd /k call start-login.bat>> build\dist\start-all.bat
echo timeout /t 5>> build\dist\start-all.bat
echo start "Game" cmd /k call start-game.bat>> build\dist\start-all.bat

echo.
echo === DONE ===
dir build\dist\AL-Login\lib | find /c ".jar"
echo jars in AL-Login\lib
dir build\dist\AL-Login
pause