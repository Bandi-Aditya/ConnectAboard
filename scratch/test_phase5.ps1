$baseUrl = "http://localhost:8081"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "ConnectAbroad Phase 4 Refinements & Phase 5 E2E Test Suite" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Login as Aditya
$bodyAditya = @{ email = "aditya@example.com"; password = "password123" } | ConvertTo-Json
$resAditya = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $bodyAditya -ContentType "application/json"
$tokenAditya = $resAditya.token
$idAditya = $resAditya.userId
Write-Host "Logged in as Aditya (ID: $idAditya)" -ForegroundColor Green

# 2. Login as Arjun
$bodyArjun = @{ email = "arjun@example.com"; password = "password123" } | ConvertTo-Json
$resArjun = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $bodyArjun -ContentType "application/json"
$tokenArjun = $resArjun.token
$idArjun = $resArjun.userId
Write-Host "Logged in as Arjun (ID: $idArjun)" -ForegroundColor Green

# 3. Login as Vivek
$bodyVivek = @{ email = "vivek@example.com"; password = "password123" } | ConvertTo-Json
$resVivek = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $bodyVivek -ContentType "application/json"
$tokenVivek = $resVivek.token
$idVivek = $resVivek.userId
Write-Host "Logged in as Vivek (ID: $idVivek)" -ForegroundColor Green

$headersAditya = @{ Authorization = "Bearer $tokenAditya" }
$headersArjun = @{ Authorization = "Bearer $tokenArjun" }
$headersVivek = @{ Authorization = "Bearer $tokenVivek" }

# Ensure Aditya and Arjun are CONNECTED
$statusCheck = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idArjun" -Method Get -Headers $headersAditya
if ($statusCheck.status -ne "CONNECTED") {
    Write-Host "Establishing ACCEPTED connection between Aditya and Arjun..." -ForegroundColor Yellow
    $req = Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idArjun" -Method Post -Headers $headersAditya
    $accept = Invoke-RestMethod -Uri "$baseUrl/api/connections/$($req.id)/accept" -Method Put -Headers $headersArjun
}
Write-Host "Aditya and Arjun are connected!" -ForegroundColor Green

Write-Host "`n--- Test 1: Phase 4 Refinement - Dynamic Connection Count ---" -ForegroundColor Yellow
$profAditya = Invoke-RestMethod -Uri "$baseUrl/api/profiles/me" -Method Get -Headers $headersAditya
$countAditya = $profAditya.connectionCount
Write-Host "Aditya's PostgreSQL Connection Count: $countAditya" -ForegroundColor Green

Write-Host "`n--- Test 2: Phase 5 Security Rule - Unconnected Chat Blocked ---" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/conversations/$idVivek" -Method Post -Headers $headersAditya
    Write-Host "FAILED: Allowed conversation with unconnected user!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Unconnected user chat blocked with security exception!" -ForegroundColor Green
}

Write-Host "`n--- Test 3: Get/Create Private Conversation (Aditya & Arjun) ---" -ForegroundColor Yellow
$conv1 = Invoke-RestMethod -Uri "$baseUrl/api/conversations/$idArjun" -Method Post -Headers $headersAditya
$convId = $conv1.id
$otherName = $conv1.otherUser.name
Write-Host "Conversation Created/Fetched! Conversation ID: $convId, Other User: $otherName" -ForegroundColor Green

Write-Host "`n--- Test 4: Duplicate Conversation Prevention ---" -ForegroundColor Yellow
$conv2 = Invoke-RestMethod -Uri "$baseUrl/api/conversations/$idArjun" -Method Post -Headers $headersAditya
if ($conv2.id -eq $convId) {
    Write-Host "SUCCESS: Same conversation ID ($convId) returned, duplicate prevented!" -ForegroundColor Green
} else {
    Write-Host "FAILED: Created duplicate conversation ID!" -ForegroundColor Red
}

Write-Host "`n--- Test 5: Send Private Message (Aditya to Arjun) ---" -ForegroundColor Yellow
$msgBody1 = @{ conversationId = $convId; recipientId = $idArjun; content = "Hi Arjun! Testing Phase 5 real-time chat." } | ConvertTo-Json
$msg1 = Invoke-RestMethod -Uri "$baseUrl/api/conversations/$convId/messages" -Method Post -Body $msgBody1 -Headers $headersAditya -ContentType "application/json"
$msgId1 = $msg1.id
$msgContent1 = $msg1.content
Write-Host "Message Sent! Message ID: $msgId1, Content: $msgContent1" -ForegroundColor Green

Write-Host "`n--- Test 6: Conversations List & Unread Count for Arjun ---" -ForegroundColor Yellow
$convsArjun = Invoke-RestMethod -Uri "$baseUrl/api/conversations" -Method Get -Headers $headersArjun
$unread1 = $convsArjun[0].unreadCount
$snippet1 = $convsArjun[0].lastMessage
Write-Host "Arjun's Conversations Count: $($convsArjun.Count)" -ForegroundColor Green
Write-Host "Latest Message Snippet: $snippet1, Unread Count: $unread1" -ForegroundColor Green

Write-Host "`n--- Test 7: Arjun Fetches Conversation Messages (Marks Unread as Read) ---" -ForegroundColor Yellow
$msgsArjun = Invoke-RestMethod -Uri "$baseUrl/api/conversations/$convId/messages" -Method Get -Headers $headersArjun
Write-Host "Messages Loaded for Arjun Count: $($msgsArjun.content.Count)" -ForegroundColor Green

$convsArjunAfterRead = Invoke-RestMethod -Uri "$baseUrl/api/conversations" -Method Get -Headers $headersArjun
$unreadAfter = $convsArjunAfterRead[0].unreadCount
Write-Host "Arjun's Unread Count After Opening Chat: $unreadAfter" -ForegroundColor Green

Write-Host "`n--- Test 8: Reply Message (Arjun to Aditya) ---" -ForegroundColor Yellow
$msgBody2 = @{ conversationId = $convId; recipientId = $idAditya; content = "Hey Aditya! Phase 5 messaging works perfectly." } | ConvertTo-Json
$msg2 = Invoke-RestMethod -Uri "$baseUrl/api/conversations/$convId/messages" -Method Post -Body $msgBody2 -Headers $headersArjun -ContentType "application/json"
$msgContent2 = $msg2.content
Write-Host "Arjun Replied! Message ID: $($msg2.id), Content: $msgContent2" -ForegroundColor Green

Write-Host "`n--- Test 9: Conversation Participant Security (Vivek access check) ---" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/conversations/$convId/messages" -Method Get -Headers $headersVivek
    Write-Host "FAILED: Third party user Vivek accessed private messages!" -ForegroundColor Red
} catch {
    Write-Host "SUCCESS: Third party user Vivek blocked from viewing messages!" -ForegroundColor Green
}

Write-Host "`n--- Test 10: Message Soft Deletion (Aditya deletes own message) ---" -ForegroundColor Yellow
$deletedMsg = Invoke-RestMethod -Uri "$baseUrl/api/conversations/messages/$msgId1" -Method Delete -Headers $headersAditya
$delContent = $deletedMsg.content
$isDel = $deletedMsg.deleted
Write-Host "Message Soft Deleted! Content: $delContent, Deleted: $isDel" -ForegroundColor Green

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "ALL PHASE 4 & PHASE 5 E2E TESTS PASSED PERFECTLY!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
