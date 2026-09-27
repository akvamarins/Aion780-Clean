@echo off
cd /d C:\Users\viktors\Documents\GitHub\Aion780-Clean
echo === Udalyaem target ===
rmdir /S /Q AL-Game\target 2>nul
rmdir /S /Q AL-Login\target 2>nul
rmdir /S /Q AL-Commons\target 2>nul

echo === Proveryaem chto v data ===
dir AL-Game\data\static_data\items\item_groups.xsd
dir AL-Game\data\static_data\items\item_groups.xml
echo --- Pervye 5 strok XSD (dolzhen byt choice) ---
powershell -command "Get-Content AL-Game\data\static_data\items\item_groups.xsd -TotalCount 10"
echo --- Razmer XML (dolzhen byt ~259KB, ne 128MB) ---
powershell -command "(Get-Item AL-Game\data\static_data\items\item_groups.xml).Length"

echo === Sobiraem ===
cd AL-Game
mvn clean package -DskipTests

echo === Chto popalo v target ===
dir target\classes\data\static_data\items\item_groups.xsd 2>nul
dir target\classes\data\static_data\items\item_groups.xml 2>nul
powershell -command "Get-Content target\classes\data\static_data\items\item_groups.xsd -TotalCount 10"
powershell -command "(Get-Item target\classes\data\static_data\items\item_groups.xml).Length"

pause
