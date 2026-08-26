# ConnectAbroad — Phase 3 Postman API Test Suite & Documentation

This test suite covers all Phase 3 endpoints for **Real People Discovery**, filtering, pagination, match reason calculations, and curated recommendation sections.

---

## 1. Create Multiple Accounts & Profiles for Discovery Testing

### Account 1 (Logged-In User: Aditya)
- **User**: `aditya@example.com` (`userType: ASPIRING`)
- **Profile**:
  - `collegeName`: `Sri Indu Institute of Engineering & Technology`
  - `hometown`: `Hyderabad, India`
  - `targetCountry`: `Canada`
  - `targetCity`: `Toronto`
  - `profession`: `Software Engineer`

### Account 2 (Target Person 1: Arjun)
- **User**: `arjun@example.com` (`userType: ABROAD`)
- **Profile**:
  - `collegeName`: `Sri Indu Institute of Engineering & Technology`
  - `hometown`: `Hyderabad, India`
  - `currentCountry`: `Canada`
  - `currentCity`: `Toronto`
  - `movedYear`: 2023
  - `profession`: `Software Engineer`

### Account 3 (Target Person 2: Priya)
- **User**: `priya@example.com` (`userType: ABROAD`)
- **Profile**:
  - `collegeName`: `Monash University`
  - `currentCountry`: `Australia`
  - `currentCity`: `Melbourne`
  - `profession`: `Data Analyst`

---

## 2. API Endpoints & Verification

### Test 1: Paginated People Discovery (`GET /api/profiles`)
- **Request**: `GET http://localhost:8081/api/profiles?page=0&size=12`
- **Headers**: `Authorization: Bearer <ADITYA_JWT>`
- **Expected Status**: `200 OK`
- **Assertions**:
  - Excludes `Aditya` (the logged in user).
  - Returns `Arjun` and `Priya`.
  - For `Arjun`, `matchReasons` contains `["✓ Same college", "✓ Same hometown", "✓ Lives in your target country", "✓ Lives in your target city", "✓ Same profession"]`.

---

### Test 2: Search by Keyword (`GET /api/profiles?keyword=Toronto`)
- **Request**: `GET http://localhost:8081/api/profiles?keyword=Toronto`
- **Headers**: `Authorization: Bearer <ADITYA_JWT>`
- **Expected Status**: `200 OK`
- **Assertions**: Returns profiles where name, college, profession, city, or country matches `"Toronto"` (e.g. `Arjun`).

---

### Test 3: Filter by College (`GET /api/profiles?college=Sri%20Indu`)
- **Request**: `GET http://localhost:8081/api/profiles?college=Sri%20Indu`
- **Headers**: `Authorization: Bearer <ADITYA_JWT>`
- **Expected Status**: `200 OK`
- **Assertions**: Returns profiles from `Sri Indu Institute` (excluding current user).

---

### Test 4: Combined Multi-Field Filters (`GET /api/profiles?currentCountry=Canada&userType=ABROAD`)
- **Request**: `GET http://localhost:8081/api/profiles?currentCountry=Canada&userType=ABROAD`
- **Headers**: `Authorization: Bearer <ADITYA_JWT>`
- **Expected Status**: `200 OK`
- **Assertions**: Returns `ABROAD` users living in `Canada`.

---

### Test 5: Section — "People From Your College" (`GET /api/profiles/sections/college`)
- **Request**: `GET http://localhost:8081/api/profiles/sections/college`
- **Headers**: `Authorization: Bearer <ADITYA_JWT>`
- **Expected Status**: `200 OK`
- **Assertions**: Returns `Arjun` (same college).

---

### Test 6: Section — "People In Your Destination" (`GET /api/profiles/sections/destination`)
- **Request**: `GET http://localhost:8081/api/profiles/sections/destination`
- **Headers**: `Authorization: Bearer <ADITYA_JWT>`
- **Expected Status**: `200 OK`
- **Assertions**: For `ASPIRING` user targeting `Canada`, returns `ABROAD` users living in `Canada` (`Arjun`).

---

### Test 7: Get Another Person's Public Profile (`GET /api/profiles/{id}`)
- **Request**: `GET http://localhost:8081/api/profiles/1`
- **Headers**: `Authorization: Bearer <ADITYA_JWT>`
- **Expected Status**: `200 OK`
- **Assertions**: Returns `Arjun`'s public social profile (`PublicProfileResponse`) with match reasons chips, excluding password, JWT, or internal role.
