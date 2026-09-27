@echo off
title AL-Game 7.8 - Java 17 FINAL STABLE
setlocal enabledelayedexpansion

:: ===== CONFIG =====
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
set "JAVA=%JAVA_HOME%\bin\java.exe"
:: auto-find dist folder
if exist "%~dp0build\dist\AL-Game\AL-Game-8.0-SNAPSHOT.jar" (
    cd /d "%~dp0build\dist\AL-Game"
) else if exist "%~dp0AL-Game-8.0-SNAPSHOT.jar" (
    cd /d "%~dp0"
) else (
    echo [ERROR] AL-Game jar not found!
    pause
    exit /b
)

:: 0 = lenient (no javaagent, siege warning but stable) | 1 = strict (with javaagent, no siege warnings)
set "USE_JAVAAGENT=0"

:: find commons jar for javaagent
set "AGENT_JAR="
for %%f in (lib\AL-Commons*.jar) do set "AGENT_JAR=%%f"
if not defined AGENT_JAR for %%f in (lib\commons*.jar) do set "AGENT_JAR=%%f"

echo ================================
echo  AL-Game 7.8 Java 17 FINAL
echo  Mode: %USE_JAVAAGENT% (0=lenient, 1=strict)
echo  Java: %JAVA%
echo  Agent: %AGENT_JAR%
echo ================================

set "JAVA_OPTS=-Xms2048m -Xmx4096m -XX:+UseG1GC -XX:+UseStringDeduplication"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.lang=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.util=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.io=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.math=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.text=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.net=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.nio=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.security=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.util.concurrent=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.sql=ALL-UNNAMED"
set "JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8"

if "%USE_JAVAAGENT%"=="1" (
    if defined AGENT_JAR (
        echo [INFO] Starting WITH javaagent - siege will auto-stop
        "%JAVA%" -javaagent:%AGENT_JAR% %JAVA_OPTS% -cp "AL-Game-8.0-SNAPSHOT.jar;lib/*" com.aionemu.gameserver.GameServer
    ) else (
        echo [WARN] Agent jar not found, fallback to lenient
        "%JAVA%" %JAVA_OPTS% -cp "AL-Game-8.0-SNAPSHOT.jar;lib/*" com.aionemu.gameserver.GameServer
    )
) else (
    echo [INFO] Starting WITHOUT javaagent - STABLE LENIENT MODE
    echo [INFO] Siege warnings will show but server stable
    "%JAVA%" %JAVA_OPTS% -cp "AL-Game-8.0-SNAPSHOT.jar;lib/*" com.aionemu.gameserver.GameServer
)

pause
