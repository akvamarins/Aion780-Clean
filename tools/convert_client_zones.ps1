$tools = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\tools'
$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$DD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\build\dist\AL-Game\data\static_data'

$maps = @(
    @('lf2', '210030000'),
    @('lf3', '210020000'),
    @('lf4', '210060000'),
    @('df2', '220030000'),
    @('df2a', '220050000'),
    @('df3', '220020000')
)

foreach ($m in $maps) {
    $zn = $m[0]; $mapId = $m[1]
    $src = "$tools\pak_world\client_world_${zn}_decoded.xml"
    if (-not (Test-Path $src)) { Write-Output "MISS $src"; continue }
    [xml]$x = Get-Content $src
    $outFile = "$SD\zones\zones_$mapId.xml"

    # keep existing custom quest zones (our handmade ones) if file exists
    $keep = ''
    if (Test-Path $outFile) {
        $old = Get-Content $outFile -Raw
        $keep = ($old -replace '<\?xml[^>]*\?>', '' -replace '(?s)</?zones[^>]*>', '').Trim()
    }

    $sb = New-Object System.Text.StringBuilder
    [void]$sb.AppendLine('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>')
    [void]$sb.AppendLine('<zones>')
    [void]$sb.AppendLine("`t<!-- retail client zones from 7.9 client (client_world_$zn) -->")
    $count = 0
    foreach ($sz in $x.clientzones.subzones.subzone) {
        $pts = $sz.points_info.points.data
        if ($null -eq $pts -or $pts.Count -lt 3) { continue }
        $name = ($sz.name).ToUpper() + '_' + $mapId
        $prio = $sz.priority
        if (-not $prio) { $prio = '7' }
        [void]$sb.AppendLine("`t<zone mapid=`"$mapId`" name=`"$name`" area_type=`"POLYGON`" zone_type=`"SUB`" priority=`"$prio`" flags=`"54`">")
        [void]$sb.AppendLine("`t`t<points bottom=`"-1000.0`" top=`"4000.0`">")
        foreach ($p in $pts) {
            [void]$sb.AppendLine("`t`t`t<point x=`"$($p.x)`" y=`"$($p.y)`"/>")
        }
        [void]$sb.AppendLine("`t`t</points>")
        [void]$sb.AppendLine("`t</zone>")
        $count++
    }
    if ($keep) {
        [void]$sb.AppendLine("`t<!-- custom quest trigger zones (kept) -->")
        [void]$sb.AppendLine($keep)
    }
    [void]$sb.AppendLine('</zones>')
    [System.IO.File]::WriteAllText($outFile, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))
    [xml]$chk = Get-Content $outFile
    Copy-Item $outFile "$DD\zones\" -Force
    Write-Output ("zones_{0}.xml: {1} retail zones{2}" -f $mapId, $count, $(if ($keep) { ' + custom quest zones kept' } else { '' }))
}
