$tools = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\tools'
$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$DD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\build\dist\AL-Game\data\static_data'

$maps = @(
    @('lf2', '210030000', @('VERTERON_CITADEL','Q1012','MYSTERIOUS_SHIPWRECK')),
    @('lf3', '210020000', @('ELTNEN_FORTRESS')),
    @('lf4', '210060000', @()),
    @('df2', '220030000', @('ALTGARD_FORTRESS','MUMU_FARMLAND','BLACK_CLAW_OUTPOST','Q2016')),
    @('df2a', '220050000', @()),
    @('df3', '220020000', @('MORHEIM_FORTRESS'))
)

foreach ($m in $maps) {
    $zn = $m[0]; $mapId = $m[1]; $customNames = $m[2]
    $src = "$tools\pak_world\client_world_${zn}_decoded.xml"
    [xml]$cx = Get-Content $src
    $outFile = "$SD\zones\zones_$mapId.xml"

    # extract custom quest zones from CURRENT file (non-suffixed variants only)
    $customBlocks = @()
    if ((Test-Path $outFile) -and ($customNames.Count -gt 0)) {
        [xml]$cur = Get-Content $outFile
        foreach ($z in $cur.zones.zone) {
            foreach ($cn in $customNames) {
                if ($z.name -eq "${cn}_${mapId}") { $customBlocks += $z.OuterXml }
            }
        }
    }

    $doc = New-Object System.Xml.XmlDocument
    [void]$doc.AppendChild($doc.CreateXmlDeclaration('1.0', 'UTF-8', 'yes'))
    $root = $doc.CreateElement('zones')
    [void]$doc.AppendChild($root)
    $seen = @{}
    $count = 0; $dupes = 0
    foreach ($sz in $cx.clientzones.subzones.subzone) {
        $pts = $sz.points_info.points.data
        if ($null -eq $pts -or $pts.Count -lt 3) { continue }
        $base = ($sz.name).ToUpper() + '_' + $mapId
        $name = $base
        if ($seen.ContainsKey($base)) { $seen[$base]++; $name = "${base}_D$($seen[$base])"; $dupes++ } else { $seen[$base] = 1 }
        $prio = $sz.priority; if (-not $prio) { $prio = '7' }
        $z = $doc.CreateElement('zone')
        $z.SetAttribute('mapid', $mapId); $z.SetAttribute('name', $name)
        $z.SetAttribute('area_type', 'POLYGON'); $z.SetAttribute('zone_type', 'SUB')
        $z.SetAttribute('priority', $prio); $z.SetAttribute('flags', '54')
        $points = $doc.CreateElement('points')
        $points.SetAttribute('bottom', '-1000.0'); $points.SetAttribute('top', '4000.0')
        foreach ($p in $pts) {
            $pt = $doc.CreateElement('point')
            $pt.SetAttribute('x', $p.x); $pt.SetAttribute('y', $p.y)
            [void]$points.AppendChild($pt)
        }
        [void]$z.AppendChild($points)
        [void]$root.AppendChild($z)
        $count++
    }
    foreach ($cb in $customBlocks) {
        $frag = $doc.CreateDocumentFragment()
        $frag.InnerXml = $cb
        [void]$root.AppendChild($frag)
    }
    $settings = New-Object System.Xml.XmlWriterSettings
    $settings.Indent = $true; $settings.IndentChars = "`t"
    $settings.Encoding = New-Object System.Text.UTF8Encoding($false)
    $w = [System.Xml.XmlWriter]::Create($outFile, $settings); $doc.Save($w); $w.Close()
    [xml]$chk = Get-Content $outFile
    Copy-Item $outFile "$DD\zones\" -Force
    Write-Output ("zones_{0}.xml: {1} retail ({2} client-dupes suffixed), {3} custom kept" -f $mapId, $count, $dupes, $customBlocks.Count)
}
