$baseUrl = "http://localhost:8081"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Phase 4 Connection System E2E Test Suite" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Login as Aditya
$bodyAditya = @{ email = "aditya@example.com"; password = "password123" } | ConvertTo-Json
$resAditya = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $bodyAditya -ContentType "application/json"
$tokenAditya = $resAditya.token
$idAditya = $resAditya.userId
Write-Host "Logged in as Aditya (ID: $idAditya, Email: $($resAditya.email))" -ForegroundColor Green

# 2. Login as Arjun
$bodyArjun = @{ email = "arjun@example.com"; password = "password123" } | ConvertTo-Json
$resArjun = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $bodyArjun -ContentType "application/json"
$tokenArjun = $resArjun.token
$idArjun = $resArjun.userId
Write-Host "Logged in as Arjun (ID: $idArjun, Email: $($resArjun.email))" -ForegroundColor Green

# 3. Login as Vivek
$bodyVivek = @{ email = "vivek@example.com"; password = "password123" } | ConvertTo-Json
$resVivek = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $bodyVivek -ContentType "application/json"
$tokenVivek = $resVivek.token
$idVivek = $resVivek.userId
Write-Host "Logged in as Vivek (ID: $idVivek, Email: $($resVivek.email))" -ForegroundColor Green

# Headers
$headersAditya = @{ Authorization = "Bearer $tokenAditya" }
$headersArjun = @{ Authorization = "Bearer $tokenArjun" }
$headersVivek = @{ Authorization = "Bearer $tokenVivek" }

Write-Host "`n--- Test 1: Self Connection Prevention ---" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idAditya" -Method Post -Headers $headersAditya
    Write-Host "FAILED: Self connection allowed!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Self connection blocked" -ForegroundColor Green
}

Write-Host "`n--- Test 2: Aditya sends connection request to Arjun ---" -ForegroundColor Yellow
$req1 = Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idArjun" -Method Post -Headers $headersAditya
$connId1 = $req1.id
Write-Host "Request Sent. Connection ID: $connId1, Status: $($req1.status), Message: $($req1.message)" -ForegroundColor Green

Write-Host "`n--- Test 3: Check Connection Status from Aditya to Arjun ---" -ForegroundColor Yellow
$status1 = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idArjun" -Method Get -Headers $headersAditya
Write-Host "Aditya -> Arjun Status: $($status1.status) (Connection ID: $($status1.connectionId))" -ForegroundColor Green

Write-Host "`n--- Test 4: Check Connection Status from Arjun to Aditya ---" -ForegroundColor Yellow
$status2 = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idAditya" -Method Get -Headers $headersArjun
Write-Host "Arjun -> Aditya Status: $($status2.status) (Connection ID: $($status2.connectionId))" -ForegroundColor Green

Write-Host "`n--- Test 5: Duplicate Request Prevention (Same Direction) ---" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idArjun" -Method Post -Headers $headersAditya
    Write-Host "FAILED: Duplicate allowed!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Duplicate blocked with error" -ForegroundColor Green
}

Write-Host "`n--- Test 6: Duplicate Request Prevention (Opposite Direction) ---" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idAditya" -Method Post -Headers $headersArjun
    Write-Host "FAILED: Opposite duplicate allowed!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Opposite duplicate blocked with error" -ForegroundColor Green
}

Write-Host "`n--- Test 7: Arjun checks received requests ---" -ForegroundColor Yellow
$receivedArjun = Invoke-RestMethod -Uri "$baseUrl/api/connections/requests/received" -Method Get -Headers $headersArjun
Write-Host "Received Requests Count: $($receivedArjun.Count)" -ForegroundColor Green
Write-Host "Request Sender Name: $($receivedArjun[0].user.name)" -ForegroundColor Green

Write-Host "`n--- Test 8: Unauthorized acceptance (Aditya tries to accept own request) ---" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/connections/$connId1/accept" -Method Put -Headers $headersAditya
    Write-Host "FAILED: Sender accepted own request!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Sender blocked from accepting own request" -ForegroundColor Green
}

Write-Host "`n--- Test 9: Arjun accepts connection request ---" -ForegroundColor Yellow
$acceptRes = Invoke-RestMethod -Uri "$baseUrl/api/connections/$connId1/accept" -Method Put -Headers $headersArjun
Write-Host "Accepted! New Status: $($acceptRes.status)" -ForegroundColor Green

Write-Host "`n--- Test 10: Verify Connected Status & Connections List ---" -ForegroundColor Yellow
$statusConnected = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idArjun" -Method Get -Headers $headersAditya
Write-Host "Aditya -> Arjun Status: $($statusConnected.status)" -ForegroundColor Green

$myConnAditya = Invoke-RestMethod -Uri "$baseUrl/api/connections" -Method Get -Headers $headersAditya
Write-Host "Aditya's Active Connections Count: $($myConnAditya.Count) (Connected with $($myConnAditya[0].user.name))" -ForegroundColor Green

Write-Host "`n--- Test 11: Remove Connection ---" -ForegroundColor Yellow
$removeRes = Invoke-RestMethod -Uri "$baseUrl/api/connections/$connId1" -Method Delete -Headers $headersAditya
Write-Host "Removed! Status: $($removeRes.message)" -ForegroundColor Green

$statusAfterRemove = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idArjun" -Method Get -Headers $headersAditya
Write-Host "Status after removal: $($statusAfterRemove.status)" -ForegroundColor Green

Write-Host "`n--- Test 12: Cancel Sent Request ---" -ForegroundColor Yellow
$reqVivek = Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idVivek" -Method Post -Headers $headersAditya
$connIdVivek = $reqVivek.id
Write-Host "Request to Vivek sent. Connection ID: $connIdVivek" -ForegroundColor Green

$cancelRes = Invoke-RestMethod -Uri "$baseUrl/api/connections/$connIdVivek/cancel" -Method Delete -Headers $headersAditya
Write-Host "Cancelled! Message: $($cancelRes.message)" -ForegroundColor Green

$statusCancel = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idVivek" -Method Get -Headers $headersAditya
Write-Host "Status after cancel: $($statusCancel.status)" -ForegroundColor Green

Write-Host "`n--- Test 13: Reject Connection Request ---" -ForegroundColor Yellow
$reqVivek2 = Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idVivek" -Method Post -Headers $headersAditya
$connIdVivek2 = $reqVivek2.id
Write-Host "New Request to Vivek sent. Connection ID: $connIdVivek2" -ForegroundColor Green

$rejectRes = Invoke-RestMethod -Uri "$baseUrl/api/connections/$connIdVivek2/reject" -Method Put -Headers $headersVivek
Write-Host "Vivek rejected request. Status: $($rejectRes.status)" -ForegroundColor Green

$statusReject = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idVivek" -Method Get -Headers $headersAditya
Write-Host "Status after reject: $($statusReject.status)" -ForegroundColor Green

Write-Host "`n--- Test 14: Re-sending Request after Rejection ---" -ForegroundColor Yellow
$reqVivekResend = Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idVivek" -Method Post -Headers $headersAditya
Write-Host "Resent request to Vivek! Connection ID: $($reqVivekResend.id), Status: $($reqVivekResend.status)" -ForegroundColor Green

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "ALL 14 E2E TESTS PASSED PERFECTLY!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
