$zip = 'C:\Users\viktors\.cline\data\sessions\session_1792948412956_0fmhc\user-attachments\d5ad6b71-15c9-4625-af0e-899366911442-Instance_Handlers_FULL_16x_7.9_Retail_NO_STUBS.zip'
$dst = 'C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\inst16'
New-Item -ItemType Directory -Path $dst -Force | Out-Null
$bytes = [System.IO.File]::ReadAllBytes($zip)
$pos = 0
$count = 0
while ($pos -lt $bytes.Length - 4) {
    if ($bytes[$pos] -eq 0x50 -and $bytes[$pos+1] -eq 0x4B -and $bytes[$pos+2] -eq 0x03 -and $bytes[$pos+3] -eq 0x04) {
        $method  = [BitConverter]::ToUInt16($bytes, $pos + 8)
        $csize   = [BitConverter]::ToUInt32($bytes, $pos + 18)
        $usize   = [BitConverter]::ToUInt32($bytes, $pos + 22)
        $nlen    = [BitConverter]::ToUInt16($bytes, $pos + 26)
        $elen    = [BitConverter]::ToUInt16($bytes, $pos + 28)
        $name    = [System.Text.Encoding]::UTF8.GetString($bytes, $pos + 30, $nlen)
        $dstart  = $pos + 30 + $nlen + $elen
        Write-Output ("entry: {0} method={1} csize={2} usize={3}" -f $name, $method, $csize, $usize)
        if ($csize -gt 0 -and $dstart + $csize -le $bytes.Length) {
            $outPath = Join-Path $dst ($name -replace '/', '\')
            if ($name.EndsWith('/')) {
                New-Item -ItemType Directory -Path $outPath -Force | Out-Null
            } else {
                New-Item -ItemType Directory -Path (Split-Path $outPath -Parent) -Force | Out-Null
                $ms = New-Object System.IO.MemoryStream(,$bytes[$dstart..($dstart + $csize - 1)])
                if ($method -eq 8) {
                    $ds = New-Object System.IO.Compression.DeflateStream($ms, [System.IO.Compression.CompressionMode]::Decompress)
                    $out = New-Object System.IO.MemoryStream
                    $ds.CopyTo($out)
                    $ds.Close()
                    [System.IO.File]::WriteAllBytes($outPath, $out.ToArray())
                } else {
                    [System.IO.File]::WriteAllBytes($outPath, $bytes[$dstart..($dstart + $csize - 1)])
                }
                $count++
            }
            $pos = $dstart + $csize
        } else {
            $pos += 4
        }
    } else {
        $pos++
    }
}
Write-Output ("extracted files: {0}" -f $count)
