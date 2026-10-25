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

$zones = @{}
$zones['800030000'] = Get-ZoneCenters "$SD\zones\zones_800030000.xml"
$zones['220110000'] = Get-ZoneCenters "$SD\zones\zones_220110000.xml"
$spawns = @{}
$spawns['800030000'] = Get-SpawnIndex '800030000'
$spawns['220110000'] = Get-SpawnIndex '220110000'

function Nearest($sp, $x, $y) {
    $best = $null; $bestD = 1e18
    foreach ($s in $sp) {
        $d = ($s[0]-$x)*($s[0]-$x) + ($s[1]-$y)*($s[1]-$y)
        if ($d -lt $bestD) { $bestD = $d; $best = $s }
    }
    if ($bestD -gt 250000) { return $null }
    return $best
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

$fixed = @{}
$fixed[647] = @(914.25464, 1630.5387, 713.3884, 0)
$fixed[706] = ZoneCoord '800030000' 'HADRADIM_FOREST'
$fixed[707] = ZoneCoord '800030000' 'STELMAN_REGION'
$fixed[708] = ZoneCoord '800030000' 'WORKSHOP_IN_THE_ICE_VALLEY'
$fixed[709] = ZoneCoord '800030000' 'WESTERN_PART_OF_THE_SILLUS_MOUNTAINS'
$fixed[710] = ZoneCoord '800030000' 'SOUTHERN_ANCIENT_FOREST'
$fixed[711] = ZoneCoord '800030000' 'GRANU_FOREST'
$fixed[712] = ZoneCoord '800030000' 'KRERUNERK_MINE'
$fixed[713] = ZoneCoord '800030000' 'EASTERN_SILLUS_MOUNTAINS'
$fixed[714] = ZoneCoord '800030000' 'RUINS_OF_SORROW'
$fixed[715] = ZoneCoord '800030000' 'FOG_SPRING'
$fixed[716] = ZoneCoord '800030000' 'TAINTED_LANDS'
$fixed[717] = $fixed[714]
$fixed[718] = ZoneCoord '800030000' 'SILLUS_MOUNTAINS'
$fixed[466] = ZoneCoord '220110000' 'LOOKOUT_POST_OF_CALYDA_CANYON'

$tlPath = "$SD\teleport_location.xml"
$lines = [System.Collections.ArrayList](Get-Content $tlPath)
$changed = 0
for ($i = 0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
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
Write-Output ("lines changed: " + $changed)
[xml]$check = Get-Content $tlPath
Write-Output "XML valid"
