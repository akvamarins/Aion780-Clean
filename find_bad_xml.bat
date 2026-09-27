@echo off
echo Poisk bityh XML/XSD nachinayushihsya s "This XML" ili "<html" ...
cd /d C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data
findstr /S /M /C:"This XML file" *.xml *.xsd > bad_files.txt
findstr /S /M /C:"<html" *.xml *.xsd >> bad_files.txt
findstr /S /M /C:"<!DOCTYPE html" *.xml *.xsd >> bad_files.txt
echo ==== BITYE FAILY ====
type bad_files.txt
echo ====
echo Pervye 2 stroki kazhdogo bitygo faila:
for /F "delims=" %%f in (bad_files.txt) do (
  echo --- %%f ---
  powershell -command "Get-Content '%%f' -TotalCount 3"
)
pause
