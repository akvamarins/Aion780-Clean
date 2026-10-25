$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$SRC = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src'
$tplRaw = Get-Content "$SD\npcs\npc_templates.xml" -Raw
$validIds = @{}
foreach ($m in [regex]::Matches($tplRaw, 'npc_id="(\d+)"')) { $validIds[$m.Groups[1].Value] = $true }

$jobs = @(
    @('ag7x_300160000_Lower Udas Temple.xml', '300160000_Lower_Udas_Temple.xml', '300160000'),
    @("ag7x_300190000_Taloc's Hollow.xml", '300190000_Talocs_Hollow.xml', '300190000')
)
foreach ($j in $jobs) {
    $in = Join-Path $SRC $j[0]
    $srcDoc = New-Object System.Xml.XmlDocument
    $srcDoc.Load($in)
    $doc = New-Object System.Xml.XmlDocument
    [void]$doc.AppendChild($doc.CreateXmlDeclaration('1.0', 'UTF-8', 'no'))
    $root = $doc.CreateElement('spawns'); [void]$doc.AppendChild($root)
    $mapEl = $doc.CreateElement('spawn_map'); $mapEl.SetAttribute('map_id', $j[2]); [void]$root.AppendChild($mapEl)
    $total = 0; $pts = 0; $dropped = @{}
    foreach ($spawnMap in $srcDoc.spawns.spawn_map) {
        foreach ($node in $spawnMap.ChildNodes) {
            if ($node.Name -ne 'spawn') { continue }
            $nid = $node.GetAttribute('npc_id')
            if (-not $validIds.ContainsKey($nid)) { $dropped[$nid] = $true; continue }
            $newSpawn = $doc.CreateElement('spawn')
            $newSpawn.SetAttribute('npc_id', $nid)
            $rt = $node.GetAttribute('respawn_time'); if (-not $rt) { $rt = '295' }
            $newSpawn.SetAttribute('respawn_time', $rt)
            foreach ($spot in $node.SelectNodes('spot')) {
                $ns = $doc.CreateElement('spot')
                $ns.SetAttribute('x', $spot.GetAttribute('x'))
                $ns.SetAttribute('y', $spot.GetAttribute('y'))
                $ns.SetAttribute('z', $spot.GetAttribute('z'))
                $ns.SetAttribute('h', $spot.GetAttribute('h'))
                [void]$newSpawn.AppendChild($ns)
                $pts++
            }
            [void]$mapEl.AppendChild($newSpawn)
            $total++
        }
    }
    $out = Join-Path "$SD\spawns\Npcs" $j[1]
    $settings = New-Object System.Xml.XmlWriterSettings
    $settings.Indent = $true; $settings.IndentChars = "`t"
    $w = [System.Xml.XmlWriter]::Create($out, $settings); $doc.Save($w); $w.Close()
    [xml]$chk = Get-Content $out
    Write-Output ("{0}: {1} groups, {2} pts, dropped {3} [{4}]" -f $j[1], $total, $pts, $dropped.Count, (($dropped.Keys | Sort-Object) -join ','))
}
