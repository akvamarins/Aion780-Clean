@echo off
echo === Iwem 128MB retail fail ===
for /R C:\Users\viktors\Documents\GitHub\Aion780-Clean %%F in (item_groups.xml) do (
  echo %%F
  powershell -command "(Get-Item '%%F').Length"
)
echo.
echo === Vse item_groups.xsd ===
for /R C:\Users\viktors\Documents\GitHub\Aion780-Clean %%F in (item_groups.xsd) do (
  echo %%F
  powershell -command "(Get-Item '%%F').Length; Get-Content '%%F' -TotalCount 5"
  echo ---
)
echo.
echo === Gde zapuskaesh GS? ===
cd
echo Tekushaya papka: %CD%
dir AL-Game\data\static_data\items\item_groups.xml
dir data\static_data\items\item_groups.xml 2>nul
dir ..\data\static_data\items\item_groups.xml 2>nul
dir C:\Aion\data\static_data\items\item_groups.xml 2>nul

echo.
echo === Proverka target ===
dir AL-Game\target\classes\data\static_data\items\item_groups.xml 2>nul
dir AL-Game\target\AL-Game.jar 2>nul
powershell -command "if (Test-Path 'AL-Game\target\AL-Game.jar') { jar tf AL-Game\target\AL-Game.jar | Select-String item_groups }"

pause
