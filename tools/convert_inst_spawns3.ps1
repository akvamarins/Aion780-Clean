$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$SRC = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src'
$tplRaw = Get-Content "$SD\npcs\npc_templates.xml" -Raw
$validIds = @{}
$nameToIds = @{}
$rankOf = @{}
foreach ($m in [regex]::Matches($tplRaw, '<npc_template\s[^>]*?npc_id="(\d+)"[^>]*?>')) {
    $tag = $m.Value; $id = $m.Groups[1].Value
    $validIds[$id] = $true
    $nm = [regex]::Match($tag, 'name="([^"]*)"').Groups[1].Value
    if ($nm) {
        if (-not $nameToIds.ContainsKey($nm)) { $nameToIds[$nm] = @() }
        $nameToIds[$nm] += $id
    }
    $rankOf[$id] = [regex]::Match($tag, 'rank="(\w+)"').Groups[1].Value + '|' + [regex]::Match($tag, 'rating="(\w+)"').Groups[1].Value
}

$jobs = @(
    @(@('ag58_301520000_Drakensphire_Depth_(Quest).xml'), '301520000_Markana.xml', '301520000'),
    @(@('ag58_301540000_Library_of_Knowledge.xml'), '301540000_Library_of_Knowledge.xml', '301540000'),
    @(@('ag58_301550000_Garden_of_Knowledge.xml'), '301550000_Garden_of_Knowledge.xml', '301550000'),
    @(@('ag58_301660000_Kroban_Base.xml'), '301660000_Kroban_Base.xml', '301660000'),
    @(@('ag58_301680000_Runatorium_Ruins.xml'), '301680000_Runatorium_Ruins.xml', '301680000'),
    @(@('ag58_301720000_Mirash_Refuge.xml'), '301720000_Mirash_Refuge.xml', '301720000'),
    @(@('ag58_302340000_Narakkalli.xml', 'ag58_302340000_Narakkalli_1.xml', 'ag58_302340000_Narakkalli_2.xml'), '302340000_Narakkalli.xml', '302340000'),
    @(@('b48inst_300160000_Lower_Udas_Temple.xml'), '300160000_Lower_Udas_Temple.xml', '300160000'),
    @(@("b48inst_300190000_Taloc's_Hollow.xml"), '300190000_Talocs_Hollow.xml', '300190000')
)

foreach ($j in $jobs) {
    $sources = $j[0]; $outName = $j[1]; $mapId = $j[2]
    $doc = New-Object System.Xml.XmlDocument
    $decl = $doc.CreateXmlDeclaration('1.0', 'UTF-8', 'no')
    [void]$doc.AppendChild($decl)
    $root = $doc.CreateElement('spawns')
    [void]$doc.AppendChild($root)
    $mapEl = $doc.CreateElement('spawn_map')
    $mapEl.SetAttribute('map_id', $mapId)
    [void]$root.AppendChild($mapEl)

    $total = 0; $pts = 0; $dropped = 0; $remapped = 0; $bosses = @{}
    $seenKeys = @{}
    foreach ($sf in $sources) {
        $in = Join-Path $SRC $sf
        if (-not (Test-Path $in)) { continue }
        $srcDoc = New-Object System.Xml.XmlDocument
        $srcDoc.Load($in)
        foreach ($spawnMap in $srcDoc.spawns.spawn_map) {
            $lastComment = ''
            foreach ($node in $spawnMap.ChildNodes) {
                if ($node.NodeType -eq 'Comment') { $lastComment = $node.Value; continue }
                if ($node.Name -ne 'spawn') { continue }
                $nid = $node.GetAttribute('npc_id')
                $useId = $nid
                if (-not $validIds.ContainsKey($nid)) {
                    $cname = ($lastComment -split '\|\|\|')[0].Trim()
                    if ($cname -and $nameToIds.ContainsKey($cname) -and $nameToIds[$cname].Count -eq 1) {
                        $useId = $nameToIds[$cname][0]
                        $remapped++
                    } else {
                        $dropped++
                        $lastComment = ''
                        continue
                    }
                }
                $imported = $doc.ImportNode($node, $true)
                if ($useId -ne $nid) { $imported.SetAttribute('npc_id', $useId) }
                if (-not $imported.GetAttribute('respawn_time')) { $imported.SetAttribute('respawn_time', '295') }
                # dedupe by npc+first spot coords
                $spot = $imported.SelectSingleNode('spot')
                $key = "$useId|$($spot.GetAttribute('x'))|$($spot.GetAttribute('y'))"
                if ($seenKeys.ContainsKey($key)) { $lastComment = ''; continue }
                $seenKeys[$key] = $true
                [void]$mapEl.AppendChild($imported)
                $total++
                $pts += $imported.SelectNodes('spot').Count
                if ($rankOf.ContainsKey($useId) -and $rankOf[$useId] -match 'HERO|LEGEND') { $bosses[$useId] = $true }
                $lastComment = ''
            }
        }
    }
    $out = Join-Path "$SD\spawns\Npcs" $outName
    $settings = New-Object System.Xml.XmlWriterSettings
    $settings.Indent = $true
    $settings.IndentChars = "`t"
    $w = [System.Xml.XmlWriter]::Create($out, $settings)
    $doc.Save($w)
    $w.Close()
    [xml]$chk = Get-Content $out
    Write-Output ("{0}: {1} groups ({2} remapped), {3} pts, dropped {4}, bosses [{5}]" -f $outName, $total, $remapped, $pts, $dropped, (($bosses.Keys | Sort-Object) -join ','))
}
