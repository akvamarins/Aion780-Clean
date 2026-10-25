$ErrorActionPreference = 'Stop'
$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'

$handlerIds = @{}
Get-ChildItem 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\scripts\system\handlers\quest' -Recurse -Filter '_*.java' | ForEach-Object {
    if ($_.BaseName -match '^_(\d+)') { $handlerIds[[int]$Matches[1]] = $true }
}

# parse quest_data.xml: id + category_name + race + name
$qx = Get-Content "$SD\quest_data\quest_data.xml" -Raw
$quests = [regex]::Matches($qx, '<quest id="(\d+)" name="([^"]*)" category_name="([^"]*)"[^>]*race_permitted="(\w+)"')
Write-Output ('templates: ' + $quests.Count + ', java handlers: ' + $handlerIds.Count)

# coverage by category
$catStats = @{}
foreach ($q in $quests) {
    $id = [int]$q.Groups[1].Value
    $cat = $q.Groups[3].Value
    if (-not $catStats.ContainsKey($cat)) { $catStats[$cat] = @{ total = 0; covered = 0 } }
    $catStats[$cat].total++
    if ($handlerIds.ContainsKey($id)) { $catStats[$cat].covered++ }
}
Write-Output ''
Write-Output '=== coverage by category (6.x/7.x content) ==='
foreach ($cat in 'Lakrum','Dumaha','Red Katalam','North Katalam','South Katalam','Akaron','Stellusia','Apsaranta','Signia','Vengar','Esterra','Nosra','Timeless Terrace') {
    if ($catStats.ContainsKey($cat)) {
        $s = $catStats[$cat]
        Write-Output ('{0,-20} {1,4} templates, {2,4} with handlers ({3}%)' -f $cat, $s.total, $s.covered, [math]::Round(100.0 * $s.covered / $s.total))
    } else {
        Write-Output ('{0,-20} ABSENT in quest_data.xml' -f $cat)
    }
}
Write-Output ''
Write-Output '=== coverage by category (old content) ==='
foreach ($cat in 'Poeta','Ishalgen','Verteron','Eltnen','Heiron','Altgard','Morheim','Beluslan','Brusthonin','Theobomos','Sanctum','Pandaemonium','Inggison','Gelkmaros','Reshanta','Sarpan','Tiamaranta') {
    if ($catStats.ContainsKey($cat)) {
        $s = $catStats[$cat]
        Write-Output ('{0,-20} {1,4} templates, {2,4} with handlers ({3}%)' -f $cat, $s.total, $s.covered, [math]::Round(100.0 * $s.covered / $s.total))
    }
}
$totalQ = 0; $totalC = 0
foreach ($k in $catStats.Keys) { $totalQ += $catStats[$k].total; $totalC += $catStats[$k].covered }
Write-Output ''
Write-Output ('TOTAL: ' + $totalC + ' / ' + $totalQ + ' quests have java handlers (' + [math]::Round(100.0 * $totalC / $totalQ, 1) + '%)')
