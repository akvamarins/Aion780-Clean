$zip = 'C:\Users\viktors\.cline\data\sessions\session_1792948412956_0fmhc\user-attachments\d5ad6b71-15c9-4625-af0e-899366911442-Instance_Handlers_FULL_16x_7.9_Retail_NO_STUBS.zip'
$dst = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\inst16'
New-Item -ItemType Directory -Path $dst -Force | Out-Null
$bytes = [System.IO.File]::ReadAllBytes($zip)

# find all local-header positions whose name looks like a file name
$positions = @()
for ($i = 0; $i -lt $bytes.Length - 30; $i++) {
    if ($bytes[$i] -eq 0x50 -and $bytes[$i+1] -eq 0x4B -and $bytes[$i+2] -eq 0x03 -and $bytes[$i+3] -eq 0x04) {
        $nlen = [BitConverter]::ToUInt16($bytes, $i + 26)
        if ($nlen -gt 0 -and $nlen -lt 200 -and ($i + 30 + $nlen) -lt $bytes.Length) {
            $name = [System.Text.Encoding]::ASCII.GetString($bytes, $i + 30, $nlen)
            if ($name -match '^[\x20-\x7E\\/]+$' -and $name -match '\.java$') {
                $positions += ,@($i, $name, $nlen)
            }
        }
    }
}
Write-Output ("candidate entries: {0}" -f $positions.Count)

for ($k = 0; $k -lt $positions.Count; $k++) {
    $i = $positions[$k][0]; $name = $positions[$k][1]; $nlen = $positions[$k][2]
    $elen = [BitConverter]::ToUInt16($bytes, $i + 28)
    $dstart = $i + 30 + $nlen + $elen
    if ($k + 1 -lt $positions.Count) { $dend = $positions[$k+1][0] } else {
        # end at central directory signature or EOF
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
        Write-Output ("{0}: {1} bytes inflated, package-ok={2}" -f $name, $arr.Length, $ok)
    } catch {
        Write-Output ("{0}: FAILED {1}" -f $name, $_.Exception.Message)
    }
}
