$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$SRC = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src'
$tplRaw = Get-Content "$SD\npcs\npc_templates.xml" -Raw
$validIds = @{}
foreach ($m in [regex]::Matches($tplRaw, 'npc_id="(\d+)"')) { $validIds[$m.Groups[1].Value] = $true }

# npc rank lookup for boss detection
$rankOf = @{}
foreach ($m in [regex]::Matches($tplRaw, 'npc_id="(\d+)"[^>]*?rank="(\w+)"')) { $rankOf[$m.Groups[1].Value] = $m.Groups[2].Value }

$jobs = @(
    @('ag58_301520000_Drakensphire_Depth_(Quest).xml', '301520000_Markana.xml'),
    @('ag58_301540000_Library_of_Knowledge.xml', '301540000_Library_of_Knowledge.xml'),
    @('ag58_301550000_Garden_of_Knowledge.xml', '301550000_Garden_of_Knowledge.xml'),
    @('ag58_301660000_Kroban_Base.xml', '301660000_Kroban_Base.xml'),
    @('ag58_301680000_Runatorium_Ruins.xml', '301680000_Runatorium_Ruins.xml'),
    @('ag58_301720000_Mirash_Refuge.xml', '301720000_Mirash_Refuge.xml'),
    @('ag58_302340000_Narakkalli.xml', '302340000_Narakkalli.xml'),
    @('b48inst_300160000_Lower_Udas_Temple.xml', '300160000_Lower_Udas_Temple.xml'),
    @("b48inst_300190000_Taloc's_Hollow.xml", '300190000_Talocs_Hollow.xml')
)

foreach ($j in $jobs) {
    $in = Join-Path $SRC $j[0]
    if (-not (Test-Path $in)) { Write-Output "SRC MISSING: $($j[0])"; continue }
    $raw = Get-Content $in -Raw
    # capture map_id
    $mm = [regex]::Match($raw, 'map_id="(\d+)"')
    if (-not $mm.Success) { Write-Output "NO MAP_ID: $($j[0])"; continue }
    $mapId = $mm.Groups[1].Value
    $out = Join-Path "$SD\spawns\Npcs" $j[1]
    $groups = [regex]::Matches($raw, '(?s)<spawn npc_id="(\d+)"[^>]*>.*?</spawn>')
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine('<?xml version="1.0" encoding="UTF-8" standalone="no"?>')
    [void]$sb.AppendLine('<spawns>')
    [void]$sb.AppendLine("`t<spawn_map map_id=`"$mapId`">")
    $total = 0; $pts = 0; $dropped = @{}
    $bosses = @()
    foreach ($g in $groups) {
        $nid = $g.Groups[1].Value
        if (-not $validIds.ContainsKey($nid)) { $dropped[$nid] = $true; continue }
        [void]$sb.AppendLine("`t`t$($g.Value -replace "`n", "`n`t`t")")
        $total++
        $pts += ([regex]::Matches($g.Value, '<spot ')).Count
        if ($rankOf.ContainsKey($nid) -and $rankOf[$nid] -match 'HERO|LEGEND|MASTER') { $bosses += $nid }
    }
    [void]$sb.AppendLine("`t</spawn_map>")
    [void]$sb.AppendLine('</spawns>')
    [System.IO.File]::WriteAllText($out, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))
    [xml]$chk = Get-Content $out
    Write-Output ("{0}: {1} groups, {2} pts, dropped {3} [{4}], bossrank: {5}" -f $j[1], $total, $pts, $dropped.Count, (($dropped.Keys | Sort-Object) -join ','), (($bosses | Sort-Object -Unique) -join ','))
}

# zones: direct copy
foreach ($z in '301520000','301540000','301550000','301660000','301680000','301720000','302340000') {
    $zf = Join-Path $SRC "ag58_zones_$z.xml"
    if (Test-Path $zf) {
        Copy-Item $zf (Join-Path "$SD\zones" "zones_$z.xml") -Force
        [xml]$zx = Get-Content (Join-Path "$SD\zones" "zones_$z.xml")
        Write-Output ("zones_{0}.xml copied ({1} zones)" -f $z, $zx.zones.zone.Count)
    }
}
