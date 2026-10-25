$sd = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data'
$dd = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\build\dist\AL-Game\data\static_data'
$new = @"
`t<!-- Labyrinth - 7.9 NEW -->
`t<instance_exit instance_id="301540000" exit_world="110070000" race="ELYOS" x="499.65002" y="219.68286" z="127.48553" h="100"/>
`t<instance_exit instance_id="301540000" exit_world="120080000" race="ASMODIANS" x="576.824" y="248.95947" z="93.51" h="16"/>
`t<instance_exit instance_id="301660000" exit_world="600200000" race="PC_ALL" x="1192.1702" y="1506.3175" z="253.7151" h="58"/>
`t<instance_exit instance_id="301680000" exit_world="600200000" race="PC_ALL" x="81.66987" y="2757.882" z="227.98903" h="112"/>
"@
foreach ($p in @("$sd\instance_exit\instance_exit.xml", "$dd\instance_exit\instance_exit.xml")) {
    $c = Get-Content $p -Raw
    $c = $c -replace '</instance_exits>', ($new + '</instance_exits>')
    [System.IO.File]::WriteAllText($p, $c, (New-Object System.Text.UTF8Encoding($false)))
    [xml]$x = Get-Content $p
    Write-Output ("{0}: {1} exits, XML valid" -f $p, $x.instance_exits.instance_exit.Count)
}
