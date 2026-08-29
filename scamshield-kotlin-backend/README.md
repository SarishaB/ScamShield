# ScamShield Kotlin Backend

Backend for the BITWISE OPERATORS ScamShield prototype.

The SIH proposal describes a multimodal scam-prevention system that accepts screenshots/SMS/Gmail/URLs/QRs, extracts content, runs URL analysis, message scam-intent analysis and community intelligence, then fuses the signals into a Low/Medium/High risk result with reasons and a safe next action. See the uploaded proposal for the original architecture and feature list.

## Architecture

Android frontend
    |
    | multipart/form-data screenshot
    v
Ktor + Kotlin/JVM
    |
    +--> OCR (Tesseract)
    |
    +--> QR decoder (ZXing)
    |
    +--> URL risk engine
    |      +--> lexical/domain heuristics
    |      +--> optional VirusTotal URL reputation
    |      +--> community reports
    |
    +--> Message scam-intent engine
    |      +--> urgency
    |      +--> OTP/credential/payment language
    |      +--> impersonation
    |      +--> investment/reward/refund
    |      +--> remote-access language
    |
    +--> Risk Fusion
    |
    +--> PostgreSQL community DB

## Important prototype behavior

1. The backend does NOT store screenshots.
2. OCR text is processed in memory and the uploaded file is deleted.
3. Community reports are normalized and hashed for exact-indicator lookup.
4. Community reports are not treated as truth until at least 3 reports corroborate the same indicator.
5. VirusTotal is optional. The server only performs a reputation lookup when `VIRUSTOTAL_API_KEY` is configured; it does not automatically submit every user URL for scanning.
6. The built-in message detector is an explainable rule engine. It is a prototype detection layer, not a production-grade ML model.
7. For production, put the server behind HTTPS, authentication, rate limiting and a reverse proxy/API gateway.

## Prerequisites

- JDK 21
- Docker + Docker Compose
- IntelliJ IDEA
- Android frontend able to send multipart HTTP
- Optional: VirusTotal API key
- If running without Docker and OCR is enabled, install the `tesseract` executable and make sure it is on PATH.

Ktor 3.5.2 is used in this project.

## Run locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the backend:

```bash
./gradlew run
```

Windows:

```powershell
gradlew.bat run
```

Health check:

```bash
curl http://localhost:8080/health
```

## Environment variables

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/scamshield
DATABASE_USER=scamshield
DATABASE_PASSWORD=scamshield

VIRUSTOTAL_API_KEY=your_key_here

# Strongly recommended outside local development
SCAMSHIELD_API_KEY=your_private_api_key

OCR_ENABLED=true
TESSERACT_COMMAND=tesseract
TESSERACT_LANGUAGE=eng
```

## API

### 1. Screenshot analysis

`POST /api/v1/analyze/screenshot`

Content type:

`multipart/form-data`

Field:

`screenshot`

Example:

```bash
curl -X POST http://localhost:8080/api/v1/analyze/screenshot \
  -H "X-API-Key: your_private_api_key" \
  -F "screenshot=@screenshot.png"
```

The response contains:

- `riskLevel`
- `riskScore`
- `reasons`
- `safeAction`
- OCR `extractedText`
- extracted URL results
- decoded QR result(s)
- message analysis
- community intelligence

### 2. Text analysis

`POST /api/v1/analyze/text`

```json
{
  "text": "URGENT: your account is suspended. Verify at https://example.com"
}
```

### 3. URL analysis

`GET /api/v1/analyze/url?url=https://example.com`

### 4. Community report

`POST /api/v1/reports`

```json
{
  "indicator": "https://example.com/fake-login",
  "type": "URL",
  "category": "phishing",
  "description": "Fake login page pretending to be a bank."
}
```

## Android/Kotlin client example

Use Android's HTTP client of choice. With Ktor Client:

```kotlin
val response = client.post("$BASE_URL/api/v1/analyze/screenshot") {
    setBody(
        MultiPartFormDataContent(
            formData {
                append(
                    "screenshot",
                    imageBytes,
                    Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=\"screenshot.png\"")
                    }
                )
            }
        )
    )
}
```

If you configure `SCAMSHIELD_API_KEY`, add:

```kotlin
header("X-API-Key", BuildConfig.SCAMSHIELD_API_KEY)
```

Do NOT hard-code a secret production API key into a publicly distributed Android APK. Use an authenticated user/session mechanism or a gateway.

## Detection logic

### URL

The prototype scores:

- HTTPS absence
- IP-address host
- Punycode
- known URL shorteners
- suspicious TLDs
- user-info syntax
- unusually long URLs
- scam-related lexical terms
- community reports
- VirusTotal reputation, if configured

### Message

The prototype scores:

- OTP / verification requests
- credential requests
- urgency
- account suspension / KYC threats
- payment requests
- rewards/refunds/prizes
- investment claims
- impersonation
- remote-access software language
- URLs and UPI-like identifiers
- community reports

### QR

The backend decodes QR codes using ZXing. If the QR contains a URL, the URL is passed through the same URL engine. If it contains `upi://`, it is classified as a UPI QR payload.

### Risk fusion

- 0–29: LOW
- 30–59: MEDIUM
- 60–100: HIGH

The final score intentionally gives high-risk signals more weight than a simple average.

## Production upgrades

Before a real public deployment, add:

- JWT/OAuth2 or device/user authentication
- per-user rate limits
- request size/rate limits at the gateway
- HTTPS only
- structured audit logging without sensitive message contents
- abuse controls for community reports
- report moderation
- ML model inference service
- multilingual OCR and message models
- fuzzy/similarity matching for scam campaigns
- URL/domain age and DNS/WHOIS signals
- Safe Browsing/threat-intelligence integrations where licensing permits
- encrypted secrets management
- database migrations using Flyway/Liquibase
- observability: metrics, traces and alerts
- automated integration tests
- Android retry/timeout handling

## Why this matches the SIH proposal

The uploaded proposal calls for URL analysis, QR decoding/OCR, message scam-intent analysis, community intelligence and risk fusion with explainable reasons and safe next actions. This backend implements those modules as separate Kotlin services so the prototype can be demonstrated end-to-end without coupling the Android UI to the detection logic.
