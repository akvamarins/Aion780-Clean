@echo off
title AL-Game - Java 17 FIXED v3 - no javaagent
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot"
set "JAVA=%JAVA_HOME%\bin\java.exe"
cd /d "%~dp0build\dist\AL-Game"

echo Starting AL-Game with Java 17...

"%JAVA%" ^
 -Xms2048m -Xmx4096m ^
 -XX:+UseG1GC -XX:+UseStringDeduplication ^
 --add-opens=java.base/java.lang=ALL-UNNAMED ^
 --add-opens=java.base/java.util=ALL-UNNAMED ^
 --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
 --add-opens=java.base/java.io=ALL-UNNAMED ^
 --add-opens=java.base/java.math=ALL-UNNAMED ^
 --add-opens=java.base/java.text=ALL-UNNAMED ^
 --add-opens=java.base/java.net=ALL-UNNAMED ^
 --add-opens=java.base/java.nio=ALL-UNNAMED ^
 --add-opens=java.base/sun.nio.ch=ALL-UNNAMED ^
 --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED ^
 --add-opens=java.base/java.security=ALL-UNNAMED ^
 --add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED ^
 --add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
 --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED ^
 -Dfile.encoding=UTF-8 ^
 -cp "AL-Game-8.0-SNAPSHOT.jar;lib/*" com.aionemu.gameserver.GameServer

pause
