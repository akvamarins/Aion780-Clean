$att = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src'
$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$DD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\build\dist\AL-Game\data\static_data'

function Convert-SpawnFile($srcFiles, $mapId, $outName) {
    $doc = New-Object System.Xml.XmlDocument
    [void]$doc.AppendChild($doc.CreateXmlDeclaration('1.0', 'UTF-8', 'no'))
    $root = $doc.CreateElement('spawns'); [void]$doc.AppendChild($root)
    $mapEl = $doc.CreateElement('spawn_map'); $mapEl.SetAttribute('map_id', $mapId); [void]$root.AppendChild($mapEl)
    $seen = @{}
    $byNpc = @{}
    foreach ($f in $srcFiles) {
        [xml]$x = Get-Content $f
        foreach ($s in $x.spawn_map.spawn) {
            $key = "{0}|{1:N1}|{2:N1}" -f $s.npc_id, [float]$s.x, [float]$s.y
            if ($seen.ContainsKey($key)) { continue }
            $seen[$key] = $true
            if (-not $byNpc.ContainsKey($s.npc_id)) { $byNpc[$s.npc_id] = @() }
            $byNpc[$s.npc_id] += ,@($s.x, $s.y, $s.z, $s.h)
        }
    }
    $total = 0; $pts = 0
    foreach ($nid in ($byNpc.Keys | Sort-Object { [int]$_ })) {
        $spawn = $doc.CreateElement('spawn')
        $spawn.SetAttribute('npc_id', $nid)
        $spawn.SetAttribute('respawn_time', '295')
        foreach ($p in $byNpc[$nid]) {
            $spot = $doc.CreateElement('spot')
            $spot.SetAttribute('x', $p[0]); $spot.SetAttribute('y', $p[1]); $spot.SetAttribute('z', $p[2]); $spot.SetAttribute('h', $p[3])
            [void]$spawn.AppendChild($spot)
            $pts++
        }
        [void]$mapEl.AppendChild($spawn)
        $total++
    }
    $out = "$SD\spawns\Npcs\$outName"
    if (Test-Path $out) { Copy-Item $out "$out.bak2" -Force }
    $settings = New-Object System.Xml.XmlWriterSettings
    $settings.Indent = $true; $settings.IndentChars = "`t"
    $w = [System.Xml.XmlWriter]::Create($out, $settings); $doc.Save($w); $w.Close()
    [xml]$chk = Get-Content $out
    Copy-Item $out "$DD\spawns\Npcs\" -Force
    Write-Output ("{0}: {1} groups, {2} pts (from {3} files)" -f $outName, $total, $pts, $srcFiles.Count)
}

# Garden of Knowledge (Ideternity-02)
Convert-SpawnFile @("$att\Ideternity-02-Spawns.xml") '301550000' '301550000_Garden_of_Knowledge.xml'

# Senekta (merge 3 variants)
Convert-SpawnFile @(
    "$att\302510000-Senekta-02.xml",
    "$att\302510000-Senekta-01.xml",
    "$att\Idinfinity-03-Spawns.xml"
) '302510000' '302510000_Senekta.xml'
