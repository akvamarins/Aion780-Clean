$zip = 'C:\Users\viktors\.cline\data\sessions\session_1792948412956_0fmhc\user-attachments\d5ad6b71-15c9-4625-af0e-899366911442-Instance_Handlers_FULL_16x_7.9_Retail_NO_STUBS.zip'
$dst = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\inst16'
New-Item -ItemType Directory -Path $dst -Force | Out-Null
$bytes = [System.IO.File]::ReadAllBytes($zip)

$entries = @()
for ($i = 0; $i -lt $bytes.Length - 34; $i++) {
    if ($bytes[$i] -eq 0x50 -and $bytes[$i+1] -eq 0x4B -and $bytes[$i+2] -eq 0x03 -and $bytes[$i+3] -eq 0x04) {
        $limit = [Math]::Min($i + 260, $bytes.Length - 5)
        for ($j = $i + 26; $j -lt $limit; $j++) {
            if ($bytes[$j] -ge 0x41 -and $bytes[$j] -le 0x7A) {
                $k = $j
                while ($k -lt $limit -and $bytes[$k] -ge 0x20 -and $bytes[$k] -le 0x7E) { $k++ }
                $run = [System.Text.Encoding]::ASCII.GetString($bytes, $j, $k - $j)
                if ($run -match '^[A-Za-z0-9_\\/]+\.java$') {
                    $entries += ,@($i, $run, $k)
                    break
                }
            }
        }
    }
}
Write-Output ("entries found: {0}" -f $entries.Count)

for ($n = 0; $n -lt $entries.Count; $n++) {
    $name = $entries[$n][1]; $dstart = $entries[$n][2]
    if ($n + 1 -lt $entries.Count) { $dend = $entries[$n+1][0] } else {
        $dend = $bytes.Length
        for ($j = $dstart; $j -lt $bytes.Length - 4; $j++) {
            if ($bytes[$j] -eq 0x50 -and $bytes[$j+1] -eq 0x4B -and $bytes[$j+2] -eq 0x01 -and $bytes[$j+3] -eq 0x02) { $dend = $j; break }
        }
    }
    $cdata = New-Object byte[] ($dend - $dstart)
    [Array]::Copy($bytes, $dstart, $cdata, 0, $cdata.Length)
    $outPath = Join-Path $dst $name
    try {
        $ms = New-Object System.IO.MemoryStream(,$cdata)
        $ds = New-Object System.IO.Compression.DeflateStream($ms, [System.IO.Compression.CompressionMode]::Decompress)
        $out = New-Object System.IO.MemoryStream
        try { $ds.CopyTo($out) } catch { }
        $ds.Close()
        $arr = $out.ToArray()
        [System.IO.File]::WriteAllBytes($outPath, $arr)
        $txt = [System.Text.Encoding]::UTF8.GetString($arr)
        $ok = $txt -match 'package instance'
        Write-Output ("{0}: {1} bytes, package-ok={2}" -f $name, $arr.Length, $ok)
    } catch {
        Write-Output ("{0}: FAILED {1}" -f $name, $_.Exception.Message)
    }
}
