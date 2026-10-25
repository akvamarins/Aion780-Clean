$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$drops = (Get-ChildItem "$SD\npc_drops" -File | Get-Content -Raw) -join ' '
$tpl = Get-Content "$SD\npcs\npc_templates.xml" -Raw
foreach ($map in @('210030000_Verteron', '210060000_Theobomos', '220030000_Altgard', '220050000_Brusthonin')) {
    $sp = Get-Content "$SD\spawns\Npcs\$map.xml" -Raw
    $ids = [regex]::Matches($sp, 'npc_id="(\d+)"') | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
    $withDrops = 0; $without = 0
    foreach ($id in $ids) {
        if ($drops -match "npc_id=`"$id`"") { $withDrops++ } else { $without++ }
    }
    $pct = [int]($withDrops * 100 / $ids.Count)
    Write-Output ("{0}: {1} unique npcs, with drops {2} ({3}%), without {4}" -f $map, $ids.Count, $withDrops, $pct, $without)
}
