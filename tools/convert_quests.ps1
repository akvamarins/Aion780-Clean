$ErrorActionPreference = 'Stop'
$SRC = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\altgard'
$DST = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\scripts\system\handlers\quest\altgard'
New-Item -ItemType Directory -Path $DST -Force | Out-Null

$files = Get-ChildItem $SRC -Filter '*.java'
foreach ($f in $files) {
    $c = Get-Content $f.FullName -Raw

    # --- engine API registrations ---
    $c = $c -replace 'qe\.setNpcQuestData\((\w+)\)\.addOnTalkEvent\(questId\)', 'qe.registerQuestNpc($1).addOnTalkEvent(questId)'
    $c = $c -replace 'qe\.setNpcQuestData\((\w+)\)\.addOnKillEvent\(questId\)', 'qe.registerQuestNpc($1).addOnKillEvent(questId)'
    $c = $c -replace 'qe\.addQuestLvlUp\(questId\)', 'qe.registerOnLevelUp(questId)'
    $c = $c -replace 'qe\.addOnEnterWorld\(questId\)', 'qe.registerOnEnterWorld(questId)'
    $c = $c -replace 'qe\.addOnDie\(questId\)', 'qe.registerOnDie(questId)'
    $c = $c -replace 'qe\.setQuestEnterZone\(ZoneName\.(\w+)\)\.add\(questId\)', 'qe.registerOnEnterZone(ZoneName.get("$1"), questId)'
    $c = $c -replace 'qe\.setQuestItem\((\d+), questId\)', 'qe.registerQuestItem($1, questId)'
    $c = $c -replace 'ZoneName\.(\w+)', 'ZoneName.get("$1")'

    # --- quest status / dialogs ---
    $c = $c -replace 'updateQuestStatus\(player, qs\)', 'updateQuestStatus(env)'
    $c = $c -replace 'sendQuestDialog\(player,\s*env\.getVisibleObject\(\)\s*\.?\s*getObjectId\(\)\s*,\s*(\w+)\)', 'sendQuestDialog(env, $1)'
    $c = $c -replace 'defaultQuestEndDialog', 'sendQuestEndDialog'
    $c = $c -replace 'defaultQuestStartDialog', 'sendQuestStartDialog'
    $c = $c -replace 'PacketSendUtility\.sendPacket\(player, new SM_DIALOG_WINDOW\(env\.getVisibleObject\(\)\s*\.?\s*getObjectId\(\)\s*,\s*10\)\);', 'closeDialogWindow(env);'

    # --- dialog ids -> DialogAction ---
    $c = $c -replace 'env\.getDialogId\(\) == 25\b', 'env.getDialog() == DialogAction.QUEST_SELECT'
    $c = $c -replace 'env\.getDialogId\(\) == 17\b', 'env.getDialog() == DialogAction.SELECTED_QUEST_NOREWARD'
    $c = $c -replace 'env\.getDialogId\(\) == 33\b', 'env.getDialog() == DialogAction.CHECK_USER_HAS_QUEST_ITEM'
    $c = $c -replace 'env\.getDialogId\(\) == 1009\b', 'env.getDialog() == DialogAction.SELECT_QUEST_REWARD'
    $c = $c -replace 'env\.getDialogId\(\) == -1\b', 'env.getDialog() == DialogAction.USE_OBJECT'
    $c = $c -replace 'switch\s*\(\s*env\.getDialogId\(\)\s*\)', 'switch(env.getDialog())'
    $c = $c -replace 'case 25:', 'case QUEST_SELECT:'
    $c = $c -replace 'case 33:', 'case CHECK_USER_HAS_QUEST_ITEM:'
    $c = $c -replace 'case 17:', 'case SELECTED_QUEST_NOREWARD:'
    $c = $c -replace 'case 1009:', 'case SELECT_QUEST_REWARD:'
    $c = $c -replace 'case -1:', 'case USE_OBJECT:'
    $c = $c -replace 'case 10000:', 'case SETPRO1:'
    $c = $c -replace 'case 10001:', 'case SETPRO2:'
    $c = $c -replace 'case 10002:', 'case SETPRO3:'
    $c = $c -replace 'case 10003:', 'case SETPRO4:'
    $c = $c -replace 'case 10004:', 'case SETPRO5:'
    $c = $c -replace 'case 10005:', 'case SETPRO6:'
    $c = $c -replace 'case 10006:', 'case SETPRO7:'
    $c = $c -replace 'case 10007:', 'case SETPRO8:'
    $c = $c -replace 'case 10008:', 'case SETPRO9:'

    # --- items / zones / spawns ---
    $c = $c -replace '!ItemService\.addItems\(player, Collections\.singletonList\(new QuestItems\((\d+), 1\)\)\)', '!giveQuestItem(env, $1, 1)'
    $c = $c -replace 'ItemService\.addItems\(player, Collections\.singletonList\(new QuestItems\((\d+), 1\)\)\)', 'giveQuestItem(env, $1, 1)'
    $c = $c -replace 'ZoneService\.getInstance\(\)\.isInsideZone\(player, ZoneName\.get\("(\w+)"\)\)', 'player.isInsideZone(ZoneName.get("$1"))'
    $c = $c -replace 'QuestService\.addNewSpawn\(([^;]+), true\)', 'QuestService.addNewSpawn($1, 30)'

    # --- add DialogAction import if needed ---
    if ($c -match 'DialogAction\.' -and $c -notmatch 'import com\.aionemu\.gameserver\.model\.DialogAction;') {
        $c = $c -replace '(import com\.aionemu\.gameserver\.model\.gameobjects)', "import com.aionemu.gameserver.model.DialogAction;`r`nimport com.aionemu.gameserver.model.gameobjects"
    }

    $out = Join-Path $DST $f.Name
    [System.IO.File]::WriteAllText($out, $c, (New-Object System.Text.UTF8Encoding($false)))
    Write-Output ("converted: " + $f.Name)
}
Write-Output 'DONE'
