$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'

# --- 1) Missing quest spawns for Theobomos / Brusthonin ---
$theobomosAdd = @'
		<spawn npc_id="203834" respawn_time="295">
			<spot x="2290.5" y="2240.2" z="62.19004" h="73"/>
		</spawn>
		<spawn npc_id="203784" respawn_time="295">
			<spot x="405.1" y="1215.4" z="134.589" h="60"/>
		</spawn>
		<spawn npc_id="700411" respawn_time="295">
			<spot x="2718.83" y="852.01" z="131.59" h="3"/>
			<spot x="2806.62" y="836.1" z="120.49" h="3"/>
		</spawn>
		<spawn npc_id="730153" respawn_time="295">
			<spot x="2715.0" y="855.0" z="131.59" h="3"/>
		</spawn>
'@
$brusthoninAdd = @'
		<spawn npc_id="730164" respawn_time="295">
			<spot x="1844.2" y="890.3" z="59.108303" h="32"/>
		</spawn>
		<spawn npc_id="204057" respawn_time="295">
			<spot x="1836.5" y="890.5" z="59.108303" h="32"/>
		</spawn>
'@

foreach ($pair in @(@("$SD\spawns\Npcs\210060000_Theobomos.xml", $theobomosAdd), @("$SD\spawns\Npcs\220050000_Brusthonin.xml", $brusthoninAdd))) {
    $p = $pair[0]; $add = $pair[1]
    $c = Get-Content $p -Raw
    $c = $c.Replace("`t</spawn_map>", $add + "`t</spawn_map>")
    [System.IO.File]::WriteAllText($p, $c, (New-Object System.Text.UTF8Encoding($false)))
    [xml]$x = Get-Content $p
    Write-Output ("{0} OK" -f (Split-Path $p -Leaf))
}

# --- 2) Adma Stronghold instance spawns from beyond-aion 4.8 ---
$tplRaw = Get-Content "$SD\npcs\npc_templates.xml" -Raw
$validIds = @{}
foreach ($m in [regex]::Matches($tplRaw, 'npc_id="(\d+)"')) { $validIds[$m.Groups[1].Value] = $true }
[xml]$x = Get-Content 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\b48_320130000_Adma_Stronghold.xml'
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('<?xml version="1.0" encoding="UTF-8" standalone="no"?>')
[void]$sb.AppendLine('<!-- Converted from beyond-aion 4.8 instance spawns -->')
[void]$sb.AppendLine('<spawns>')
[void]$sb.AppendLine("`t<spawn_map map_id=`"320130000`">")
$total = 0; $dropped = @{}
foreach ($sp in $x.spawns.spawn_map.spawn) {
    $nid = $sp.npc_id
    if (-not $validIds.ContainsKey($nid)) { $dropped[$nid] = $true; continue }
    $rt = $sp.respawn_time
    if (-not $rt) { $rt = '295' }
    $first = $true
    foreach ($o in $sp.spot) {
        if ($first) {
            [void]$sb.AppendLine("`t`t<spawn npc_id=`"$nid`" respawn_time=`"$rt`">")
            $first = $false
        }
        [void]$sb.AppendLine("`t`t`t<spot x=`"$($o.x)`" y=`"$($o.y)`" z=`"$($o.z)`" h=`"$($o.h)`"/>")
    }
    if (-not $first) { [void]$sb.AppendLine("`t`t</spawn>"); $total++ }
}
[void]$sb.AppendLine("`t</spawn_map>")
[void]$sb.AppendLine('</spawns>')
$out = "$SD\spawns\Npcs\320130000_Adma_Stronghold.xml"
[System.IO.File]::WriteAllText($out, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))
[xml]$chk = Get-Content $out
Write-Output ("320130000_Adma_Stronghold.xml: {0} spawn groups, dropped: {1}" -f $total, (($dropped.Keys | Sort-Object) -join ','))

# --- 3) world map check ---
$wm = Get-Content "$SD\world\world_map_template.xml" -Raw -ErrorAction SilentlyContinue
if ($wm -match '320130000') { Write-Output 'world map 320130000 OK' } else { Write-Output 'WORLD MAP 320130000 MISSING' }
