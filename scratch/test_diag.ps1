$baseUrl = "http://localhost:8081"
$b = @{ email = "aditya@example.com"; password = "password123" } | ConvertTo-Json
$r = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $b -ContentType "application/json"
$t = $r.token
Write-Host "Token: $t"

$h = @{ Authorization = "Bearer $t" }

Write-Host "Testing GET /api/profiles/me..."
try {
    $me = Invoke-RestMethod -Uri "$baseUrl/api/profiles/me" -Method Get -Headers $h
    Write-Host "SUCCESS me: $($me.name)" -ForegroundColor Green
} catch {
    Write-Host "ERROR me: $_" -ForegroundColor Red
}

Write-Host "Testing GET /api/profiles/search?keyword=Toronto..."
try {
    $search = Invoke-RestMethod -Uri "$baseUrl/api/profiles/search?keyword=Toronto" -Method Get -Headers $h
    Write-Host "SUCCESS search: $($search.content.Count) items found" -ForegroundColor Green
} catch {
    Write-Host "ERROR search: $_" -ForegroundColor Red
}

Write-Host "Testing GET /api/profiles/7..."
try {
    $p7 = Invoke-RestMethod -Uri "$baseUrl/api/profiles/7" -Method Get -Headers $h
    Write-Host "SUCCESS p7: $($p7.name)" -ForegroundColor Green
} catch {
    Write-Host "ERROR p7: $_" -ForegroundColor Red
}

Write-Host "Testing GET /api/profiles/people..."
try {
    $peop = Invoke-RestMethod -Uri "$baseUrl/api/profiles/people" -Method Get -Headers $h
    Write-Host "SUCCESS people: $($peop.content.Count) items found" -ForegroundColor Green
} catch {
    Write-Host "ERROR people: $_" -ForegroundColor Red
}
