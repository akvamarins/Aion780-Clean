$ErrorActionPreference = 'Stop'
$SRC = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src'
$TPL = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data\npcs\npc_templates.xml'
$OUTDIR = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data\spawns\Npcs'

# load valid npc ids
$tplRaw = Get-Content $TPL -Raw
$validIds = @{}
foreach ($m in [regex]::Matches($tplRaw, 'npc_id="(\d+)"')) { $validIds[$m.Groups[1].Value] = $true }

function Convert-Map($mapId, $outName) {
    $npcFile = Join-Path $SRC "${mapId}_npcs.xml"
    $monFile = Join-Path $SRC "${mapId}_monsters.xml"
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine('<?xml version="1.0" encoding="UTF-8" standalone="no"?>')
    [void]$sb.AppendLine('<!-- Converted from aion-unique 2.x spawns (xtnm repo) -->')
    [void]$sb.AppendLine('<spawns>')
    [void]$sb.AppendLine("`t<spawn_map map_id=`"$mapId`">")
    $total = 0; $dropped = @{}
    foreach ($f in @($npcFile, $monFile)) {
        if (-not (Test-Path $f)) { continue }
        [xml]$x = Get-Content $f
        foreach ($sp in $x.spawns.spawn) {
            $nid = $sp.npcid
            if (-not $validIds.ContainsKey($nid)) { $dropped[$nid] = $true; continue }
            $interval = $sp.interval
            if (-not $interval) { $interval = '295' }
            foreach ($obj in $sp.object) {
                [void]$sb.AppendLine("`t`t<spawn npc_id=`"$nid`" respawn_time=`"$interval`">")
                [void]$sb.AppendLine("`t`t`t<spot x=`"$($obj.x)`" y=`"$($obj.y)`" z=`"$($obj.z)`" h=`"$($obj.h)`"/>")
                [void]$sb.AppendLine("`t`t</spawn>")
                $total++
            }
        }
    }
    [void]$sb.AppendLine("`t</spawn_map>")
    [void]$sb.AppendLine('</spawns>')
    $outPath = Join-Path $OUTDIR $outName
    [System.IO.File]::WriteAllText($outPath, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))
    [xml]$check = Get-Content $outPath
    Write-Output ("{0}: {1} spawn points written, {2} npc ids dropped (missing in npc_templates)" -f $outName, $total, $dropped.Count)
    if ($dropped.Count -gt 0) { Write-Output ('dropped ids: ' + (($dropped.Keys | Sort-Object) -join ', ')) }
}

Convert-Map '210030000' '210030000_Verteron.xml'
Convert-Map '220030000' '220030000_Altgard.xml'
