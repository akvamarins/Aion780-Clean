$ErrorActionPreference = 'Stop'
$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'

function Get-ZoneCenters($zoneFile) {
    $map = @{}
    if (-not (Test-Path $zoneFile)) { return $map }
    [xml]$z = Get-Content $zoneFile
    foreach ($zone in $z.zones.zone) {
        $name = $zone.name -replace '_\d{9}$', ''
        $sx = 0.0; $sy = 0.0; $n = 0
        foreach ($p in $zone.points.point) { $sx += [double]$p.x; $sy += [double]$p.y; $n++ }
        if ($n -gt 0) { $map[$name] = @{ x = [math]::Round($sx/$n,2); y = [math]::Round($sy/$n,2); z = [double]$zone.points.bottom } }
    }
    return $map
}

function Get-SpawnIndex($mapId) {
    $list = @()
    foreach ($f in (Get-ChildItem "$SD\spawns" -Recurse -Filter "$mapId*.xml" -ErrorAction SilentlyContinue)) {
        foreach ($m in ([regex]::Matches((Get-Content $f.FullName -Raw), '<spot x="([\d.\-]+)" y="([\d.\-]+)" z="([\d.\-]+)" h="(\d+)"'))) {
            $list += ,(@([double]$m.Groups[1].Value, [double]$m.Groups[2].Value, [double]$m.Groups[3].Value, [int]$m.Groups[4].Value))
        }
    }
    return ,$list
}

function Nearest($spawns, $x, $y) {
    $best = $null; $bestD = 1e18
    foreach ($s in $spawns) {
        $d = ($s[0]-$x)*($s[0]-$x) + ($s[1]-$y)*($s[1]-$y)
        if ($d -lt $bestD) { $bestD = $d; $best = $s }
    }
    if ($bestD -gt 250000) { return $null }
    return $best
}

$zones = @{}
foreach ($zf in '800030000','800050000','800060000','210050000','220070000','600010000','600040000','302500000') {
    $zones[$zf] = Get-ZoneCenters "$SD\zones\zones_$zf.xml"
}
$spawns = @{}
foreach ($m in '800030000','800050000','800060000','210050000','220070000','600010000') {
    $spawns[$m] = Get-SpawnIndex $m
    Write-Output ("spawns {0}: {1}" -f $m, $spawns[$m].Count)
}

function ZoneCoord($mapId, $zoneName) {
    $zmap = $zones[$mapId]
    $key = $null
    if ($zmap.ContainsKey($zoneName)) { $key = $zoneName }
    else { $key = ($zmap.Keys | Where-Object { $_ -like "$zoneName*" } | Select-Object -First 1) }
    if (-not $key) { throw "zone not found: $mapId $zoneName" }
    $zc = $zmap[$key]
    $sp = Nearest $spawns[$mapId] $zc.x $zc.y
    if ($sp) { return @($sp[0], $sp[1], $sp[2], $sp[3]) }
    return @($zc.x, $zc.y, $zc.z, 0)
}

# ---------- fix table ----------
$fixed = @{}
$zoneFix = @{}
$fixed[612] = @(2849.852, 745.5688, 861.24335, 40)
$fixed[613] = @(2824.852, 745.5688, 861.24335, 90)
$fixed[614] = @(2874.852, 745.5688, 861.24335, 30)
$fixed[615] = @(593.7594, 776.0127, 732.2478, 113)
$fixed[616] = @(558.909, 2523.9722, 653.3905, 0)
$fixed[617] = @(1437.916, 1274.5927, 619.16437, 90)
$fixed[618] = @(2257.0852, 1164.483, 761.56085, 0)
$fixed[619] = @(177.50447, 1654.5674, 762.5662, 0)
$fixed[620] = @(2428.632, 319.63394, 657.54346, 0)
$fixed[621] = @(2876.4912, 2261.4883, 720.6205, 0)
$fixed[622] = @(1779.9381, 2812.5857, 640.0104, 0)
$fixed[623] = @(1538.187, 695.56445, 582.1799, 0)
$fixed[624] = @(2294.4822, 1921.3497, 753.7683, 0)
$fixed[625] = @(1224.9175, 2491.9153, 576.72565, 0)
$fixed[641] = @(1525.7808, 881.04364, 561.6207, 3)
$fixed[642] = @(1323.1533, 2339.513, 656.3629, 0)
$fixed[643] = @(2596.677, 1919.9534, 721.2205, 0)
$fixed[644] = @(2690.8777, 485.8917, 323.3768, 0)
$fixed[645] = @(2927.6543, 2505.6543, 313.80035, 0)
$fixed[646] = @(914.25464, 1630.5387, 713.3884, 0)
foreach ($l in 555,556) { $zoneFix[$l] = @{map='800050000'; zone='LAKRUM_FORTRESS'} }
foreach ($l in 579,585) { $zoneFix[$l] = @{map='800050000'; zone='JUNGLE_OF_SILENCE'} }
foreach ($l in 580,586) { $zoneFix[$l] = @{map='800050000'; zone='OLD_TEMPLE_OF_RECORDS'} }
foreach ($l in 581,587) { $zoneFix[$l] = @{map='800050000'; zone='PLACE_OF_SCATTERED_RECORDS'} }
foreach ($l in 582,588) { $zoneFix[$l] = @{map='800050000'; zone='ANCIENT_REDWOOD_FOREST'} }
foreach ($l in 583,589) { $zoneFix[$l] = @{map='800050000'; zone='CRUEL_COASTLINE'} }
foreach ($l in 584,590) { $zoneFix[$l] = @{map='800050000'; zone='TEMPLE_OF_ENDORIM'} }
$zoneFix[626] = @{map='800060000'; zone='EXITUS_CAMP'}
$zoneFix[627] = @{map='800060000'; zone='OUTLAW_NORTH'}
$zoneFix[628] = @{map='800060000'; zone='HEART_OF_SNUWA'}
$zoneFix[629] = @{map='800060000'; zone='REGA_GUARD_POST'}
$ords = '1ST','2ND','3RD','4TH','5TH','6TH','7TH','8TH','9TH','10TH','11TH'
for ($i = 0; $i -lt 11; $i++) { $zoneFix[630 + $i] = @{map='800060000'; zone=('AREA_AROUND_THE_' + $ords[$i] + '_ALTAR')} }
$zoneFix[719] = @{map='800030000'; zone='TAINTED_LANDS'}
$zoneFix[720] = @{map='800030000'; zone='MARCHING_ROUTE_OF_THE_GOLD_FEATHER_LEGION'}
$zoneFix[721] = @{map='800030000'; zone='COLD_VALLEY'}
$gords = '701ST','702ND','703RD','704TH','705TH','706TH','707TH','708TH','709TH','710TH','711TH','712TH','713TH'
for ($i = 0; $i -lt 13; $i++) {
    $zoneFix[804 + $i*3] = @{map='800030000'; zone=('LDF5A_' + $gords[$i] + '_GARRISON')}
    $zoneFix[843 + $i*3] = @{map='800030000'; zone=('LDF5A_' + $gords[$i] + '_GARRISON')}
}
$zoneFix[882] = @{map='210050000'; zone='INGGISON_OUTPOST'}
$zoneFix[883] = @{map='210050000'; zone='ABANDONED_TEMPLE_OF_SCALES'}
$zoneFix[884] = @{map='210050000'; zone='ABANDONED_ALTAR_OF_AVARICE'}
$zoneFix[885] = @{map='220070000'; zone='GELKMAROS_DEFENCE_BASE'}
$zoneFix[886] = @{map='220070000'; zone='ABANDONED_VORGALTEM_CITADEL'}
$zoneFix[887] = @{map='220070000'; zone='ABANDONED_CRIMSON_TEMPLE'}

foreach ($k in @($zoneFix.Keys)) {
    $zf = $zoneFix[$k]
    $fixed[$k] = ZoneCoord $zf.map $zf.zone
}

# ---------- apply to teleport_location.xml ----------
$tlPath = "$SD\teleport_location.xml"
$lines = [System.Collections.ArrayList](Get-Content $tlPath)
$changed = 0
for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    if ($line -match '^\s*<!--\s*CHECK\s*$' -or $line -match '^\s*-->\s*$') { $lines[$i] = $null; $changed++; continue }
    $m = [regex]::Match($line, 'loc_id="(\d+)"')
    if ($m.Success) {
        $lid = [int]$m.Groups[1].Value
        if ($fixed.ContainsKey($lid)) {
            $c = $fixed[$lid]
            $new = [regex]::Replace($line, 'posX="[^"]*"', ('posX="{0}"' -f $c[0]))
            $new = [regex]::Replace($new, 'posY="[^"]*"', ('posY="{0}"' -f $c[1]))
            $new = [regex]::Replace($new, 'posZ="[^"]*"', ('posZ="{0}"' -f $c[2]))
            $new = [regex]::Replace($new, 'heading="[^"]*"', ('heading="{0}"' -f $c[3]))
            $new = $new -replace '\s*<!-- CHECK POSITION -->', ''
            $lines[$i] = $new; $changed++
        }
    }
}
$out = ($lines | Where-Object { $_ -ne $null }) -join "`r`n"
[System.IO.File]::WriteAllText($tlPath, $out + "`r`n", (New-Object System.Text.UTF8Encoding($false)))
Write-Output ("teleport_location.xml lines changed: " + $changed)
[xml]$check = Get-Content $tlPath
Write-Output ("XML valid, telelocs: " + $check.teleport_location.teleloc_template.Count)

# ---------- portal_loc.xml: add missing locs ----------
$plPath = "$SD\portals\portal_loc.xml"
$pl = Get-Content $plPath -Raw
# fix Kaldor world id (600200000 does not exist)
$pl = $pl -replace 'world_id="600200000"', 'world_id="800020000"'

$ing = ZoneCoord '210050000' 'INGGISON_ILLUSION_FORTRESS'
$gel = ZoneCoord '220070000' 'GELKMAROS_FORTRESS'
$sw  = ZoneCoord '600010000' 'SILENTERA_WESTGATE_1'
$se  = ZoneCoord '600010000' 'SILENTERA_EASTGATE_1'
$eb  = ZoneCoord '600040000' 'ELYOS_BREAKOUT'
$ab  = ZoneCoord '600040000' 'ASMODIAN_BREAKOUT'
$hr  = ZoneCoord '302500000' 'GALLERY_OF_DESIRE'
$t719 = $fixed[719]; $t720 = $fixed[720]

$newLocs = @"
    <!-- === ADDED: missing portal locations (new maps & capitals) === -->
    <portal_loc world_id="110010000" loc_id="1100100" x="1313.25" y="1512.011" z="568.107" h="0"/>
    <portal_loc world_id="110010000" loc_id="1100102" x="1930.9808" y="1514.3209" z="590.01074" h="60"/>
    <portal_loc world_id="110020000" loc_id="1100200" x="2183.665" y="1552.2333" z="1204.9952" h="75"/>
    <portal_loc world_id="110070000" loc_id="1100700" x="479.40924" y="279.1825" z="127.51457" h="90"/>
    <portal_loc world_id="110070000" loc_id="1100702" x="479.40924" y="279.1825" z="127.51457" h="90"/>
    <portal_loc world_id="120010000" loc_id="1200100" x="1685.7" y="1400.5" z="195.48618" h="60"/>
    <portal_loc world_id="120080000" loc_id="1200800" x="528.83246" y="269.6238" z="93.88659" h="115"/>
    <portal_loc world_id="210040000" loc_id="2100400" x="2542.2327" y="339.2828" z="411.66394" h="36"/>
    <portal_loc world_id="210050000" loc_id="2100500" x="$($ing[0])" y="$($ing[1])" z="$($ing[2])" h="$($ing[3])"/>
    <portal_loc world_id="210070000" loc_id="2100700" x="2947.6506" y="855.2476" z="572.7626" h="0"/>
    <portal_loc world_id="220040000" loc_id="2200400" x="398.3" y="400.8" z="222.79721" h="23"/>
    <portal_loc world_id="220070000" loc_id="2200700" x="$($gel[0])" y="$($gel[1])" z="$($gel[2])" h="$($gel[3])"/>
    <portal_loc world_id="220080000" loc_id="2200800" x="427.91165" y="2231.3667" z="220.37758" h="0"/>
    <portal_loc world_id="600010000" loc_id="6000101" x="$($sw[0])" y="$($sw[1])" z="$($sw[2])" h="$($sw[3])"/>
    <portal_loc world_id="600010000" loc_id="6000102" x="$($se[0])" y="$($se[1])" z="$($se[2])" h="$($se[3])"/>
    <portal_loc world_id="800020000" loc_id="6002000" x="557.6848" y="2176.164" z="284.76025" h="100"/>
    <portal_loc world_id="800020000" loc_id="6002001" x="557.6848" y="2176.164" z="284.76025" h="100"/>
    <portal_loc world_id="700010000" loc_id="7000101" x="1178.576" y="1853.733" z="99.750" h="18"/>
    <portal_loc world_id="710010000" loc_id="7100100" x="1129.402" y="1476.817" z="100.250" h="65"/>
    <portal_loc world_id="800030000" loc_id="8000300" x="$($t719[0])" y="$($t719[1])" z="$($t719[2])" h="$($t719[3])"/>
    <portal_loc world_id="800030000" loc_id="8000301" x="$($t720[0])" y="$($t720[1])" z="$($t720[2])" h="$($t720[3])"/>
    <portal_loc world_id="300920000" loc_id="3009200" x="1476.7037" y="249.04787" z="243.45805" h="30"/>
    <portal_loc world_id="302430000" loc_id="3024301" x="881.74805" y="771.0323" z="757.54047" h="60"/>
    <portal_loc world_id="302500000" loc_id="3025000" x="$($hr[0])" y="$($hr[1])" z="$($hr[2])" h="$($hr[3])"/>
    <portal_loc world_id="302520000" loc_id="3025200" x="459.66248" y="407.5619" z="129.76265" h="21"/>
    <portal_loc world_id="800080000" loc_id="8000801" x="$($eb[0])" y="$($eb[1])" z="$($eb[2])" h="$($eb[3])"/>
    <portal_loc world_id="800080000" loc_id="8000802" x="$($ab[0])" y="$($ab[1])" z="$($ab[2])" h="$($ab[3])"/>
"@
$pl = $pl -replace '</portal_locs>', ($newLocs + '</portal_locs>')
[System.IO.File]::WriteAllText($plPath, $pl, (New-Object System.Text.UTF8Encoding($false)))
[xml]$plc = Get-Content $plPath
Write-Output ("portal_loc.xml valid, locs: " + $plc.portal_locs.portal_loc.Count)

