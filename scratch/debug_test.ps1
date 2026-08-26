$baseUrl = "http://localhost:8081"
$resAditya = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body (@{ email = "aditya@example.com"; password = "password123" } | ConvertTo-Json) -ContentType "application/json"
$tokenAditya = $resAditya.token

$resArjun = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body (@{ email = "arjun@example.com"; password = "password123" } | ConvertTo-Json) -ContentType "application/json"
$tokenArjun = $resArjun.token
$idArjun = $resArjun.user.id

Write-Host "Aditya ID: $($resAditya.user.id), Arjun ID: $idArjun"

$headersAditya = @{ Authorization = "Bearer $tokenAditya" }
$headersArjun = @{ Authorization = "Bearer $tokenArjun" }

try {
    $req1 = Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idArjun" -Method Post -Headers $headersAditya
    Write-Host "Req1:" ($req1 | ConvertTo-Json)
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "Req Error Body:" $reader.ReadToEnd()
}

try {
    $acceptRes = Invoke-RestMethod -Uri "$baseUrl/api/connections/$($req1.id)/accept" -Method Put -Headers $headersArjun
    Write-Host "AcceptRes:" ($acceptRes | ConvertTo-Json)
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "Accept Error Body:" $reader.ReadToEnd()
}
