$SD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\AL-Game\data\static_data\zones'
$DD = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\build\dist\AL-Game\data\static_data\zones'
$files = @('zones_210030000.xml','zones_210020000.xml','zones_210060000.xml','zones_220030000.xml','zones_220050000.xml','zones_220020000.xml')
foreach ($f in $files) {
    $p = Join-Path $SD $f
    [xml]$x = Get-Content $p
    $seen = @{}
    $renamed = 0
    foreach ($z in $x.zones.zone) {
        $name = $z.name
        if ($seen.ContainsKey($name)) {
            $seen[$name]++
            $z.name = "${name}_D$($seen[$name])"
            $renamed++
        } else {
            $seen[$name] = 1
        }
    }
    if ($renamed -gt 0) {
        $settings = New-Object System.Xml.XmlWriterSettings
        $settings.Indent = $true
        $settings.IndentChars = "`t"
        $settings.Encoding = New-Object System.Text.UTF8Encoding($false)
        $w = [System.Xml.XmlWriter]::Create($p, $settings)
        $x.Save($w)
        $w.Close()
        Copy-Item $p (Join-Path $DD $f) -Force
    }
    Write-Output ("{0}: {1} duplicates renamed" -f $f, $renamed)
}
