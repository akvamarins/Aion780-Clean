$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$SRC = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src'
$tplRaw = Get-Content "$SD\npcs\npc_templates.xml" -Raw
$validIds = @{}
$nameToIds = @{}
$rankOf = @{}
foreach ($m in [regex]::Matches($tplRaw, '(?s)<npc_template[^>]*?npc_id="(\d+)"[^>]*?>')) {
    $tag = $m.Value
    $id = $m.Groups[1].Value
    $validIds[$id] = $true
    $nm = [regex]::Match($tag, 'name="([^"]*)"').Groups[1].Value
    if ($nm) {
        if (-not $nameToIds.ContainsKey($nm)) { $nameToIds[$nm] = @() }
        $nameToIds[$nm] += $id
    }
    $rk = [regex]::Match($tag, 'rank="(\w+)"').Groups[1].Value
    if ($rk) { $rankOf[$id] = $rk }
}

# jobs: source file(s) -> output
$jobs = @(
    @(@('ag58_301520000_Drakensphire_Depth_(Quest).xml'), '301520000_Markana.xml'),
    @(@('ag58_301540000_Library_of_Knowledge.xml'), '301540000_Library_of_Knowledge.xml'),
    @(@('ag58_301550000_Garden_of_Knowledge.xml'), '301550000_Garden_of_Knowledge.xml'),
    @(@('ag58_301660000_Kroban_Base.xml'), '301660000_Kroban_Base.xml'),
    @(@('ag58_301680000_Runatorium_Ruins.xml'), '301680000_Runatorium_Ruins.xml'),
    @(@('ag58_301720000_Mirash_Refuge.xml'), '301720000_Mirash_Refuge.xml'),
    @(@('ag58_302340000_Narakkalli.xml', 'ag58_302340000_Narakkalli_1.xml', 'ag58_302340000_Narakkalli_2.xml'), '302340000_Narakkalli.xml'),
    @(@('b48inst_300160000_Lower_Udas_Temple.xml'), '300160000_Lower_Udas_Temple.xml'),
    @(@("b48inst_300190000_Taloc's_Hollow.xml"), '300190000_Talocs_Hollow.xml')
)

foreach ($j in $jobs) {
    $sources = $j[0]; $outName = $j[1]
    $mapId = $null
    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine('<?xml version="1.0" encoding="UTF-8" standalone="no"?>')
    [void]$sb.AppendLine('<!-- converted from AionGermany 5.8 / beyond-aion 4.8 with name-remap to 7.9 ids -->')
    [void]$sb.AppendLine('<spawns>')
    $body = New-Object System.Text.StringBuilder
    $total = 0; $pts = 0; $dropped = @(); $remapped = 0; $bosses = @()
    foreach ($sf in $sources) {
        $in = Join-Path $SRC $sf
        if (-not (Test-Path $in)) { continue }
        $raw = Get-Content $in -Raw
        if (-not $mapId) { $mapId = [regex]::Match($raw, 'map_id="(\d+)"').Groups[1].Value }
        # iterate comment+spawn pairs
        $pairs = [regex]::Matches($raw, '(?s)(?:<!--\s*(.*?)\s*-->\s*)?<spawn npc_id="(\d+)"[^>]*>.*?</spawn>')
        foreach ($p in $pairs) {
            $comment = $p.Groups[1].Value
            $nid = $p.Groups[2].Value
            $useId = $nid
            if (-not $validIds.ContainsKey($nid)) {
                # try remap by comment name (before |||)
                $cname = ($comment -split '\|\|\|')[0].Trim()
                if ($cname -and $nameToIds.ContainsKey($cname) -and $nameToIds[$cname].Count -eq 1) {
                    $useId = $nameToIds[$cname][0]
                    $remapped++
                } else {
                    $dropped += ("{0}({1})" -f $nid, $cname)
                    continue
                }
            }
            $block = $p.Value
            if ($useId -ne $nid) { $block = $block -replace "npc_id=`"$nid`"", "npc_id=`"$useId`"" }
            [void]$body.AppendLine("`t`t$($block -replace "`n", "`n`t`t")")
            $total++
            $pts += ([regex]::Matches($block, '<spot ')).Count
            if ($rankOf.ContainsKey($useId) -and $rankOf[$useId] -match 'HERO|LEGEND|MASTER') { $bosses += $useId }
        }
    }
    $sb2 = New-Object System.Text.StringBuilder
    [void]$sb2.Append($sb.ToString())
    [void]$sb2.AppendLine("`t<spawn_map map_id=`"$mapId`">")
    [void]$sb2.Append($body.ToString())
    [void]$sb2.AppendLine("`t</spawn_map>")
    [void]$sb2.AppendLine('</spawns>')
    $out = Join-Path "$SD\spawns\Npcs" $outName
    [System.IO.File]::WriteAllText($out, $sb2.ToString(), (New-Object System.Text.UTF8Encoding($false)))
    [xml]$chk = Get-Content $out
    Write-Output ("{0}: {1} groups ({2} remapped), {3} pts, dropped {4}, bossrank [{5}]" -f $outName, $total, $remapped, $pts, $dropped.Count, (($bosses | Sort-Object -Unique) -join ','))
    if ($dropped.Count -gt 0 -and $dropped.Count -le 12) { Write-Output ("   dropped: {0}" -f ($dropped -join ', ')) }
}
