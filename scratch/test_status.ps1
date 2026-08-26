$baseUrl = "http://localhost:8081"
$resAditya = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body (@{ email = "aditya@example.com"; password = "password123" } | ConvertTo-Json) -ContentType "application/json"
$tokenAditya = $resAditya.token
$resArjun = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body (@{ email = "arjun@example.com"; password = "password123" } | ConvertTo-Json) -ContentType "application/json"
$idArjun = $resArjun.id

$headersAditya = @{ Authorization = "Bearer $tokenAditya" }

Write-Host "Target Arjun ID: $idArjun"

try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idArjun" -Method Get -Headers $headersAditya
    Write-Host "Success Response:" ($res | ConvertTo-Json)
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host "Error Body:" $reader.ReadToEnd()
}
