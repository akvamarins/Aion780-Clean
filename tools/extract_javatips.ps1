$files = @(
    @('theobomos','_1091ARequestFromAtropos'), @('theobomos','_1092JosnacksDilemma'),
    @('theobomos','_1093TheCalydonRuins'), @('theobomos','_1094ProjectDrakanhammer'),
    @('brusthonin','_2091MeettheReaperSquad'), @('brusthonin','_2092GravesoftheRedSkyLegion'),
    @('brusthonin','_2093TheSecretPassage'), @('brusthonin','_2094TheSecretofAdmaStronghold'))
foreach ($q in $files) {
    $dir = $q[0]; $f = $q[1]
    $p = "C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\$dir\$f.java.html"
    $h = Get-Content $p -Raw
    $start = $h.IndexOf('package quest')
    if ($start -lt 0) { Write-Output "NOSTART $f"; continue }
    # end marker: first '</pre' or '&lt;/' after code; javatips wraps code then closes tag
    $rest = $h.Substring($start)
    $end = $rest.IndexOf('</')
    if ($end -lt 0) { $end = $rest.Length }
    $code = $rest.Substring(0, $end)
    $code = [System.Net.WebUtility]::HtmlDecode($code)
    $code = $code -replace "`r`n", "`n" -replace "`r", "`n"
    $out = "C:\Users\viktors\Documents\GitHub\Aion780-Clean\_spawn_src\$dir\$f.java"
    [System.IO.File]::WriteAllText($out, $code.Trim() + "`n", (New-Object System.Text.UTF8Encoding($false)))
    Write-Output ("{0}: {1} lines" -f $f, ($code -split "`n").Count)
}
