:: FIX dummy AI in npc_templates.xml - запусти этот .bat в папке с npc_templates.xml
@echo off
echo Fixing 1008 dummy AI...
powershell -Command "(Get-Content npc_templates.xml) -replace 'ai=\"dummy\"', 'ai=\"general\"' | Set-Content npc_templates.xml"
echo Done! Replaced dummy -> general
pause
