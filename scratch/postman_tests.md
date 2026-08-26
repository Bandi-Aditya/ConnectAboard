# ConnectAbroad — Phase 2 Postman API Test Suite & Documentation

This test suite covers all 10 Phase 2 acceptance criteria for the **Real User Profile System**.

---

## Environment Variables Setup in Postman

Set the following collection variables in Postman:
- `baseUrl`: `http://localhost:8081`
- `jwtToken`: `(populated dynamically after login)`
- `aspiringUserId`: `(ID of aspiring user)`
- `abroadUserId`: `(ID of abroad user)`

---

## 1. Registration & Authentication (Prerequisite)

### Endpoint: `POST {{baseUrl}}/api/auth/register`
**Body (JSON)**:
```json
{
  "name": "Aditya Bandi",
  "email": "aditya@example.com",
  "password": "Password123!",
  "userType": "ASPIRING"
}
```
**Expected Status**: `201 Created`

### Endpoint: `POST {{baseUrl}}/api/auth/login`
**Body (JSON)**:
```json
{
  "email": "aditya@example.com",
  "password": "Password123!"
}
```
**Tests Script (Postman)**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.collectionVariables.set("jwtToken", jsonData.token);
});
```

---

## 2. Test Cases for Phase 2 Profile System

### Test 1: Create Profile while Authenticated
**Request**: `POST {{baseUrl}}/api/profiles`  
**Headers**: `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`  
**Body (JSON)**:
```json
{
  "name": "Aditya Bandi",
  "bio": "Aspiring CS student preparing for Master's in Canada.",
  "collegeName": "Sri Indu Institute of Engineering & Technology",
  "collegeCity": "Hyderabad",
  "collegeState": "Telangana",
  "collegeCountry": "India",
  "degree": "B.Tech Computer Science",
  "graduationYear": 2025,
  "hometown": "Hyderabad, India",
  "currentCountry": "India",
  "currentCity": "Hyderabad",
  "targetCountry": "Canada",
  "targetCity": "Toronto",
  "targetUniversity": "University of Toronto",
  "expectedMoveDate": "2025-09-01",
  "profession": "Final Year Student",
  "experienceYears": 0,
  "skills": "Java, Spring Boot, SQL, Git"
}
```
**Expected Status**: `201 Created`  
**Assertions**: Response contains `id`, `userId`, `userType = "ASPIRING"`, `collegeName = "Sri Indu..."`, `profileCompletion > 0`.

---

### Test 2: Create Profile without JWT (Unauthenticated)
**Request**: `POST {{baseUrl}}/api/profiles`  
**Headers**: `Content-Type: application/json` *(No Authorization header)*  
**Body (JSON)**: Same as Test 1  
**Expected Status**: `401 Unauthorized`  
**Assertions**: Error response `Authentication Failed` / `401`.

---

### Test 3: Attempt Duplicate Profile Creation
**Request**: `POST {{baseUrl}}/api/profiles`  
**Headers**: `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`  
**Body (JSON)**: Same as Test 1  
**Expected Status**: `400 Bad Request`  
**Assertions**: Error message contains `"Profile already exists for user"`.

---

### Test 4: Get Own Profile (`GET /api/profiles/me`)
**Request**: `GET {{baseUrl}}/api/profiles/me`  
**Headers**: `Authorization: Bearer {{jwtToken}}`  
**Expected Status**: `200 OK`  
**Assertions**: Returns full profile details, user email, userType, `profileCompletion`, and `completionChecklist` breakdown. Does NOT expose password or security credentials.

---

### Test 5: Update Own Profile (`PUT /api/profiles/me`)
**Request**: `PUT {{baseUrl}}/api/profiles/me`  
**Headers**: `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`  
**Body (JSON)**:
```json
{
  "bio": "Updated bio: Passions in backend development and cloud microservices.",
  "profession": "Software Engineer Intern",
  "experienceYears": 1,
  "skills": "Java, Spring Boot, Microservices, PostgreSQL, Docker"
}
```
**Expected Status**: `200 OK`  
**Assertions**: Updated bio, profession, experienceYears, and updated skills returned in response.

---

### Test 6: Get Another User's Public Profile (`GET /api/profiles/{id}`)
**Request**: `GET {{baseUrl}}/api/profiles/1`  
**Headers**: `Authorization: Bearer {{jwtToken}}`  
**Expected Status**: `200 OK`  
**Assertions**: Returns public profile details (`PublicProfileResponse`) without exposing email, password, JWT, or internal role details.

---

### Test 7: Unauthorized Profile Modification Prevention
**Rule**: User identity is bound strictly to the Security Context (`Authentication.getName()`).  
**Verification**: Clients cannot supply a `userId` field in `POST /api/profiles` or `PUT /api/profiles/me` to modify another user's profile. Attempting to modify another profile by id is rejected at controller level.

---

### Test 8: Test Validation Errors
**Request**: `POST /api/profiles` or `PUT /api/profiles/me`  
**Headers**: `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`  
**Body (JSON)** (Invalid data):
```json
{
  "graduationYear": 1800,
  "experienceYears": -5,
  "bio": "(A string longer than 1000 characters...)"
}
```
**Expected Status**: `400 Bad Request`  
**Assertions**: Response contains field validation errors for `graduationYear`, `experienceYears`, and `bio`.

---

### Test 9: Test ABROAD Profile Setup
**User Registration**: User with `userType: "ABROAD"`  
**Request**: `POST {{baseUrl}}/api/profiles`  
**Body (JSON)**:
```json
{
  "name": "Arjun Reddy",
  "bio": "Software engineer in Toronto.",
  "collegeName": "Sri Indu Institute of Engineering & Technology",
  "graduationYear": 2021,
  "hometown": "Hyderabad, India",
  "currentCountry": "Canada",
  "currentCity": "Toronto",
  "movedYear": 2023,
  "profession": "Software Engineer",
  "experienceYears": 3,
  "skills": "Java, Spring Boot, React"
}
```
**Expected Status**: `201 Created`  
**Assertions**: `userType = "ABROAD"`, `movedYear = 2023`, `completionChecklist.journey = true`.

---

### Test 10: Test ASPIRING Profile Setup & Completion Score
**User Registration**: User with `userType: "ASPIRING"`  
**Request**: `GET {{baseUrl}}/api/profiles/me`  
**Expected Status**: `200 OK`  
**Assertions**: Verify completion calculation accounts for target country, target city, expected move date, and college details.
