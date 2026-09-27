@echo off
cd /d C:\Users\viktors\Documents\GitHub\Aion780-Clean
echo Poisk VSEH item_groups.xml vnutri Aion780-Clean (vklyuchaya vlozhennye papki)...
echo.
for /R %%F in (item_groups.xml) do (
  if exist "%%F" (
    echo NAIDEN: %%F
    for %%A in ("%%F") do echo Razmer: %%~zA byte
    echo ---
  )
)
echo.
echo === Proverka papki item vnutri items (tam tozhe mozhet lezhat staryi) ===
dir /S /B AL-Game\data\static_data\items\*.xml | findstr item_groups
dir /S AL-Game\data\static_data\items\ | findstr item_groups
echo.
echo === Vse XML bolshe 50MB v Aion780-Clean ===
forfiles /P AL-Game\data /M *.xml /S /C "cmd /c if @fsize GTR 50000000 echo @path @fsize"
echo.
pause
