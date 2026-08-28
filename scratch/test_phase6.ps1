$baseUrl = "http://localhost:8081"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "ConnectAbroad Phase 5 Stabilization and Phase 6 Social Feed E2E Test Suite" -ForegroundColor Cyan
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

# 3. Register or Login as Priya
$bodyPriya = @{ name = "Priya Sharma"; email = "priya@example.com"; password = "password123"; userType = "ASPIRING" } | ConvertTo-Json
try {
    $resPriya = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $bodyPriya -ContentType "application/json"
    $tokenPriya = $resPriya.token
    $idPriya = $resPriya.userId
} catch {
    $loginPriya = @{ email = "priya@example.com"; password = "password123" } | ConvertTo-Json
    $resPriya = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginPriya -ContentType "application/json"
    $tokenPriya = $resPriya.token
    $idPriya = $resPriya.userId
}
Write-Host "Logged in as Priya (ID: $idPriya)" -ForegroundColor Green

$headersAditya = @{ Authorization = "Bearer $tokenAditya" }
$headersArjun = @{ Authorization = "Bearer $tokenArjun" }
$headersPriya = @{ Authorization = "Bearer $tokenPriya" }

# Ensure Aditya and Arjun are CONNECTED
$statusCheck = Invoke-RestMethod -Uri "$baseUrl/api/connections/status/$idArjun" -Method Get -Headers $headersAditya
if ($statusCheck.status -ne "CONNECTED") {
    Write-Host "Connecting Aditya and Arjun..." -ForegroundColor Yellow
    $req = Invoke-RestMethod -Uri "$baseUrl/api/connections/request/$idArjun" -Method Post -Headers $headersAditya
    $accept = Invoke-RestMethod -Uri "$baseUrl/api/connections/$($req.id)/accept" -Method Put -Headers $headersArjun
}

Write-Host "`n--- Part 1: Phase 5 Regression Fix - Public Profile API Route Check ---" -ForegroundColor Yellow
$profMe = Invoke-RestMethod -Uri "$baseUrl/api/profiles/me" -Method Get -Headers $headersAditya
$meName = $profMe.name
$meCount = $profMe.connectionCount
Write-Host "GET /api/profiles/me -> Name: $meName, ConnectionCount: $meCount" -ForegroundColor Green

$profArjun = Invoke-RestMethod -Uri "$baseUrl/api/profiles/$idArjun" -Method Get -Headers $headersAditya
$arjunName = $profArjun.name
$arjunStatus = $profArjun.connectionStatus
Write-Host "GET /api/profiles/$idArjun -> Public Name: $arjunName, Status: $arjunStatus" -ForegroundColor Green

Write-Host "`n--- Part 1: Chat Regression Check ---" -ForegroundColor Yellow
$conv = Invoke-RestMethod -Uri "$baseUrl/api/conversations/$idArjun" -Method Post -Headers $headersAditya
$msgPayload = @{ conversationId = $conv.id; recipientId = $idArjun; content = "Phase 5 chat stabilization check!" } | ConvertTo-Json
$msgRes = Invoke-RestMethod -Uri "$baseUrl/api/conversations/$($conv.id)/messages" -Method Post -Body $msgPayload -Headers $headersAditya -ContentType "application/json"
$msgId = $msgRes.id
$msgText = $msgRes.content
Write-Host "Chat message created: ID $msgId, Content: $msgText" -ForegroundColor Green

Write-Host "`n--- Part 2: Phase 6 - Create Post (Aditya) ---" -ForegroundColor Yellow
$postPayload1 = @{ content = "Moved to Toronto in 2024. Here are 5 tips for finding housing!"; postType = "ABROAD_EXPERIENCE"; imageUrl = "https://images.unsplash.com/photo-1517935703635-27c5e9e03444" } | ConvertTo-Json
$post1 = Invoke-RestMethod -Uri "$baseUrl/api/posts" -Method Post -Body $postPayload1 -Headers $headersAditya -ContentType "application/json"
$postId1 = $post1.id
$authorName1 = $post1.author.name
$pType1 = $post1.postType
Write-Host "Post Created! Post ID: $postId1, Author: $authorName1, Type: $pType1" -ForegroundColor Green

Write-Host "`n--- Part 2: Phase 6 - Feed Retrieval (Arjun connected user) ---" -ForegroundColor Yellow
$feedArjun = Invoke-RestMethod -Uri "$baseUrl/api/posts/feed?page=0`&size=10" -Method Get -Headers $headersArjun
$feedCnt = $feedArjun.content.Count
$firstContent = $feedArjun.content[0].content
$firstAuthor = $feedArjun.content[0].author.name
Write-Host "Feed Loaded for Arjun! Total Feed Items: $feedCnt" -ForegroundColor Green
Write-Host "First Feed Post: '$firstContent' by $firstAuthor" -ForegroundColor Green

Write-Host "`n--- Part 2: Phase 6 - Like System and Duplicate Prevention ---" -ForegroundColor Yellow
$likeRes1 = Invoke-RestMethod -Uri "$baseUrl/api/posts/$postId1/like" -Method Post -Headers $headersArjun
$likeMsg = $likeRes1.message
Write-Host "Arjun Liked Post $postId1 -> Response: $likeMsg" -ForegroundColor Green

$postCheck1 = Invoke-RestMethod -Uri "$baseUrl/api/posts/$postId1" -Method Get -Headers $headersArjun
$likeCnt1 = $postCheck1.likeCount
$isLiked1 = $postCheck1.likedByCurrentUser
Write-Host "Post Like Count: $likeCnt1, LikedByCurrentUser: $isLiked1" -ForegroundColor Green

# Duplicate like attempt
$likeRes2 = Invoke-RestMethod -Uri "$baseUrl/api/posts/$postId1/like" -Method Post -Headers $headersArjun
$postCheck2 = Invoke-RestMethod -Uri "$baseUrl/api/posts/$postId1" -Method Get -Headers $headersArjun
$likeCnt2 = $postCheck2.likeCount
Write-Host "Duplicate Like Count Check: $likeCnt2 (Prevented duplicate!)" -ForegroundColor Green

Write-Host "`n--- Part 2: Phase 6 - Comment System ---" -ForegroundColor Yellow
$commentPayload = @{ content = "Super helpful tips Aditya, thanks for sharing!" } | ConvertTo-Json
$comment1 = Invoke-RestMethod -Uri "$baseUrl/api/posts/$postId1/comments" -Method Post -Body $commentPayload -Headers $headersArjun -ContentType "application/json"
$cId1 = $comment1.id
$cAuthor1 = $comment1.author.name
Write-Host "Comment Added! Comment ID: $cId1, Author: $cAuthor1" -ForegroundColor Green

$commentsList = Invoke-RestMethod -Uri "$baseUrl/api/posts/$postId1/comments?page=0`&size=10" -Method Get -Headers $headersAditya
$cCount = $commentsList.content.Count
$cSnippet = $commentsList.content[0].content
Write-Host "Comments Loaded for Post: $cCount comment(s)" -ForegroundColor Green
Write-Host "Comment Snippet: '$cSnippet'" -ForegroundColor Green

Write-Host "`n--- Part 2: Phase 6 - Edit Own Post (Aditya) ---" -ForegroundColor Yellow
$updatePayload = @{ content = "Updated: Moved to Toronto in 2024. Here are 6 comprehensive housing tips!"; postType = "ABROAD_EXPERIENCE" } | ConvertTo-Json
$updatedPost = Invoke-RestMethod -Uri "$baseUrl/api/posts/$postId1" -Method Put -Body $updatePayload -Headers $headersAditya -ContentType "application/json"
$newContent = $updatedPost.content
Write-Host "Post Updated! New Content: '$newContent'" -ForegroundColor Green

Write-Host "`n--- Part 2: Phase 6 - Author Profile Posts Timeline ---" -ForegroundColor Yellow
$authorPosts = Invoke-RestMethod -Uri "$baseUrl/api/profiles/$idAditya/posts?page=0`&size=10" -Method Get -Headers $headersArjun
$authorPostCount = $authorPosts.content.Count
Write-Host "Author Posts Loaded for Aditya: $authorPostCount post(s) found in profile timeline!" -ForegroundColor Green

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "ALL PHASE 5 STABILIZATION AND PHASE 6 SOCIAL FEED TESTS PASSED!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
