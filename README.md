# ScamShield

ScamShield is a scam-prevention system for Android. It accepts screenshots, SMS/text messages, URLs, and QR codes, extracts their content, runs multi-signal analysis, and fuses the results into a **Low / Medium / High** risk verdict with human-readable reasons and a recommended safe action.

The project is built by **BITWISE OPERATORS** for SIH and is composed of two modules:

| Module | Description |
|---|---|
| `frontend-app/` | Android app (Jetpack Compose, Kotlin) |
| `scamshield-kotlin-backend/` | Ktor/JVM backend (Kotlin) |

---

## How it works

```
Android app
    │
    │  multipart/form-data  (screenshot / text / URL)
    ▼
Ktor + Kotlin/JVM backend
    ├── OCR.Space cloud OCR          (screenshot → text)
    ├── ZXing QR decoder
    ├── URL risk engine
    │       ├── Lexical / domain heuristics
    │       ├── VirusTotal reputation (optional)
    │       └── Community reports
    ├── Message scam-intent engine
    │       └── Google Gemini (gemini-2.5-flash)
    ├── Risk Fusion  →  LOW / MEDIUM / HIGH
    └── PostgreSQL community database
```

---

## Android App (`frontend-app/`)

**Requirements**

- Android 10+ (API 29), targeting API 36
- arm64-v8a or armeabi-v7a device

**Key capabilities**

- Home screen with one-tap scan (text, URL, or image)
- Accessibility service — lets ScamShield analyse content from any other app
- Floating scanner overlay — always-on shield bubble over other apps
- Quick-Settings tile trigger
- On-device OCR (Tesseract4Android) and QR decoding (ZXing)

**Build**

```bash
cd frontend-app
./gradlew assembleDebug
```

---

## Backend (`scamshield-kotlin-backend/`)

### Prerequisites

- JDK 21
- Docker + Docker Compose
- API keys: OCR.Space, Google Gemini (Google AI Studio)
- Optional: VirusTotal API key

### Run locally

```bash
# 1. Start PostgreSQL
cd scamshield-kotlin-backend
docker compose up -d postgres

# 2. Export required keys
export GEMINI_API_KEY="your_google_ai_studio_key"
export OCR_SPACE_API_KEY="your_ocr_space_key"

# 3. Start the server
./gradlew run
```

Windows: replace `./gradlew` with `gradlew.bat`.

Health check:

```bash
curl http://localhost:8080/health
```

### Environment variables

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/scamshield
DATABASE_USER=scamshield
DATABASE_PASSWORD=scamshield

GEMINI_API_KEY=your_google_ai_studio_key
GEMINI_MODEL=gemini-2.5-flash          # optional override
GEMINI_BASE_URL=                        # optional override

OCR_ENABLED=true
OCR_SPACE_API_KEY=your_ocr_space_key
OCR_SPACE_BASE_URL=https://api.ocr.space/parse/image
OCR_LANGUAGE=eng
OCR_ENGINE=2

VIRUSTOTAL_API_KEY=your_key_here        # optional

SCAMSHIELD_API_KEY=your_private_api_key # strongly recommended outside local dev
```

### API endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/analyze/screenshot` | Analyse a screenshot (`multipart/form-data`, field `screenshot`) |
| `POST` | `/api/v1/analyze/text` | Analyse SMS / message text (`{"text": "..."}`) |
| `GET`  | `/api/v1/analyze/url?url=...` | Analyse a URL |
| `POST` | `/api/v1/reports` | Submit a community scam report |
| `GET`  | `/health` | Health check |

Protect all endpoints with `X-API-Key: <SCAMSHIELD_API_KEY>` when the key is configured.

### Detection logic

**URL** — scores HTTPS absence, IP-address host, Punycode, URL shorteners, suspicious TLDs, unusual length, scam lexical terms, community reports, VirusTotal reputation.

**Message** — Google Gemini scores OTP/credential requests, urgency, account threats, payment requests, prize/reward language, impersonation, remote-access language, extracted URLs and UPI identifiers, community reports.

**QR** — decoded with ZXing; URLs are passed through the URL engine; `upi://` payloads are classified as UPI QR.

**Risk fusion**

| Score | Risk level |
|---|---|
| 0 – 29 | 🟢 LOW |
| 30 – 59 | 🟡 MEDIUM |
| 60 – 100 | 🔴 HIGH |

High-risk signals are weighted above average to reduce false negatives.

### Tests

```bash
cd scamshield-kotlin-backend
./gradlew test
```

---

## Repository layout

```
ScamShield/
├── app/                          # Shared data-layer module
├── frontend-app/                 # Android Compose application
│   └── app/src/main/java/…
└── scamshield-kotlin-backend/    # Ktor/JVM backend
    ├── src/
    ├── Dockerfile
    └── docker-compose.yml
```

---

## License

Copyright © 2025 AKS-Labs. Licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html).

> **Disclaimer:** ScamShield provides risk signals, not guarantees. Never share OTPs, PINs, passwords, or banking credentials with anyone.
