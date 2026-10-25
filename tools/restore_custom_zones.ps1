$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data\zones'
$DD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\build\dist\AL-Game\data\static_data\zones'

function Z($mapId, $name, $x1, $y1, $x2, $y2, $b, $t) {
    return @"
`t<zone mapid="$mapId" name="${name}_$mapId" area_type="POLYGON" zone_type="SUB" priority="7" flags="55">
`t`t<points bottom="$b" top="$t">
`t`t`t<point x="$x1" y="$y1"/>
`t`t`t<point x="$x2" y="$y1"/>
`t`t`t<point x="$x2" y="$y2"/>
`t`t`t<point x="$x1" y="$y2"/>
`t`t</points>
`t</zone>
"@
}

$blocks = @{}
$blocks['210030000'] = "`r`n`t<!-- custom quest trigger zones (restored) -->" + `
    (Z '210030000' 'VERTERON_CITADEL' '1600.0' '1420.0' '1760.0' '1560.0' '100.0' '260.0') + `
    (Z '210030000' 'Q1012' '1850.0' '1800.0' '2110.0' '2110.0' '100.0' '300.0') + `
    (Z '210030000' 'MYSTERIOUS_SHIPWRECK' '2000.0' '500.0' '2260.0' '760.0' '50.0' '200.0')
$blocks['220030000'] = "`r`n`t<!-- custom quest trigger zones (restored, approx) -->" + `
    (Z '220030000' 'ALTGARD_FORTRESS' '1500.0' '1600.0' '1900.0' '2000.0' '150.0' '400.0') + `
    (Z '220030000' 'MUMU_FARMLAND' '1900.0' '2300.0' '2300.0' '2650.0' '150.0' '400.0') + `
    (Z '220030000' 'BLACK_CLAW_OUTPOST' '2000.0' '900.0' '2400.0' '1300.0' '150.0' '400.0') + `
    (Z '220030000' 'Q2016' '2100.0' '1000.0' '2300.0' '1200.0' '150.0' '400.0')
$blocks['210020000'] = "`r`n`t<!-- custom quest trigger zones (restored, approx) -->" + `
    (Z '210020000' 'ELTNEN_FORTRESS' '300.0' '2600.0' '500.0' '2850.0' '200.0' '400.0')
$blocks['220020000'] = "`r`n`t<!-- custom quest trigger zones (restored, approx) -->" + `
    (Z '220020000' 'MORHEIM_FORTRESS' '200.0' '2150.0' '450.0' '2400.0' '380.0' '550.0')

foreach ($mapId in $blocks.Keys) {
    foreach ($p in @((Join-Path $SD "zones_$mapId.xml"), (Join-Path $DD "zones_$mapId.xml"))) {
        $c = Get-Content $p -Raw
        if ($c -match 'VERTERON_CITADEL|ALTGARD_FORTRESS|ELTNEN_FORTRESS|MORHEIM_FORTRESS') { continue }
        $c = $c -replace '</zones>', ($blocks[$mapId] + "</zones>")
        [System.IO.File]::WriteAllText($p, $c, (New-Object System.Text.UTF8Encoding($false)))
        [xml]$x = Get-Content $p
        Write-Output ("{0}: {1} zones" -f (Split-Path $p -Leaf), $x.zones.zone.Count)
    }
}
