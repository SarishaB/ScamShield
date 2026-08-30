<div align="center">

<img src="frontend-app/app/src/main/ic_launcher-playstore.png" alt="ScamShield logo" width="120"/>

# ScamShield

<br/>

[![Android](https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Android%20%2B%20Backend-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Ktor](https://img.shields.io/badge/Backend-Ktor-000000?logo=ktor&logoColor=white)](https://ktor.io/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Status](https://img.shields.io/badge/Status-Prototype-8A5CF5)](#project-status)

<br/>
</div>

ScamShield is a scam-prevention system for Android. It accepts screenshots, SMS/text messages, URLs, and QR codes, extracts their content, runs multi-signal analysis, and fuses the results into a **Low / Medium / High** risk verdict with human-readable reasons and a recommended safe action.

The project is built by **BITWISE OPERATORS** for SIH and is composed of two modules:

| Module | Description |
|---|---|
| `frontend-app/` | Android app (Jetpack Compose, Kotlin) |
| `scamshield-kotlin-backend/` | Ktor/JVM backend (Kotlin) |

---


## The problem

Scams do not always arrive as obviously malicious files or websites.

They arrive as a **WhatsApp message**, a shortened link, a fake offer, a QR code, a screenshot, a payment request or a convincing page inside an app the user already trusts. The dangerous moment is often the few seconds **before the user clicks, pays, replies or shares sensitive information**.

Most security tools force the user to leave that context, copy content elsewhere and manually investigate it.

**ScamShield is designed to bring the check to the suspicious content instead.**

---


## What ScamShield does

ScamShield gives Android users a fast second opinion on suspicious digital content.

Users can scan **text, URLs and images directly inside the app**, or invoke ScamShield over another app using its floating scanner / accessibility-based selection workflow. The selected content can then be examined for scam indicators, suspicious URLs and QR codes before a unified risk result is returned.

Instead of only showing a binary warning, ScamShield returns:

- a **LOW / MEDIUM / HIGH** risk level,
- a **0–100 risk score**,
- plain-language **reasons** behind the assessment, and
- a **safe next action** the user can take.

> **ScamShield provides risk signals, not a guarantee.** Users should never share OTPs, PINs, passwords or banking credentials based only on an automated result.

---

## Key features

<table>
<tr>
<td width="50%">

### 🛡️ Scan in context
Invoke ScamShield while using another Android app and inspect suspicious content without manually retyping it.

### 📝 Text analysis
Analyze suspicious messages for patterns such as urgency, credential requests, payment language, impersonation and remote-access prompts.

### 🔗 URL analysis
Inspect URL structure and risk signals such as disguised hosts, shorteners, suspicious TLDs, risky keywords and other deceptive patterns.

### 🖼️ Screenshot analysis
Process screenshot/image input and combine extracted signals into a single risk assessment.

</td>
<td width="50%">

### 🔎 OCR + QR extraction
Extract text from images and detect QR payloads so hidden links or payment-style content can enter the same analysis pipeline.

### 👥 Community intelligence
Accept community reports and use corroborating reports as an additional signal rather than automatically treating a single report as truth.

### 🌐 Optional threat intelligence
URL analysis can be enriched with VirusTotal reputation data when an API key is configured.

### 🧠 Explainable risk fusion
Combine multiple signals into one score while preserving readable reasons and an actionable safety recommendation.

</td>
</tr>
</table>



## 📂 Project structure

```text
ScamShield/
├── frontend-app/
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/akslabs/circletosearch/
│   │       │   ├── MainActivity.kt
│   │       │   ├── UnifiedScanScreen.kt
│   │       │   ├── ScamShieldHomeScreenV2.kt
│   │       │   ├── CircleToSearchAccessibilityService.kt
│   │       │   ├── data/
│   │       │   ├── ocr/
│   │       │   └── ui/
│   │       └── res/
│   └── gradle/
│
├── scamshield-kotlin-backend/
│   ├── src/main/kotlin/com/bitwiseoperators/scamshield/
│   │   ├── Application.kt
│   │   ├── config/
│   │   ├── db/
│   │   ├── model/
│   │   ├── routes/
│   │   └── services/
│   │       ├── MessageAnalysisService.kt
│   │       ├── UrlAnalysisService.kt
│   │       ├── OcrService.kt
│   │       ├── QrDecoderService.kt
│   │       ├── CommunityService.kt
│   │       └── RiskFusionService.kt
│   ├── Dockerfile
│   └── docker-compose.yml
│
└── README.md
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

- JDK 24
- PostgreSQL
- API keys: OCR.Space, Google Gemini (Google AI Studio)
- Optional: VirusTotal API key

### Run locally

```bash
# 1. Start the server
./gradlew run
```

Windows: replace `./gradlew` with `gradlew.bat`.

Health check:

```bash
curl http://localhost:8080/health
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

**Risk fusion**

| Score | Risk level |
|---|---|
| 0 – 29 | 🟢 LOW |
| 30 – 59 | 🟡 MEDIUM |
| 60 – 100 | 🔴 HIGH |

High-risk signals are weighted above average to reduce false negatives.

---

## Future scope

- Multilingual scam detection, including Indian languages and Hinglish
- Evaluation on a larger reviewed scam / benign dataset
- Calibrated precision, recall, F1 and false-positive reporting
- Stronger learned text-classification models alongside the explainable rule layer
- Expanded threat-intelligence and domain-reputation sources
- Privacy-preserving similarity matching for community reports
- Improved QR / UPI-specific risk analysis
- Secure authentication, rate limiting and production deployment hardening
- Better abuse/spam controls for community submissions
- Continuous detection-rule and threat-signal updates

---


# Demos

https://github.com/user-attachments/assets/9c40493d-28bf-4ccd-8b48-4adfa5f7ea00


https://github.com/user-attachments/assets/b92aaba1-9497-49b9-87be-59def6b5c508


https://github.com/user-attachments/assets/8557cb96-0606-4298-af21-839b075df97a

---

## 🙌 Acknowledgements

ScamShield uses and builds on open-source technologies including:

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/compose)
- [Ktor](https://ktor.io/)
- [Tesseract OCR](https://github.com/tesseract-ocr/tesseract)
- [ZXing](https://github.com/zxing/zxing)
- [PostgreSQL](https://www.postgresql.org/)
- [VirusTotal](https://www.virustotal.com/) for optional URL reputation enrichment

The Android interaction layer contains work derived from / based on the open-source [AKS-Labs CircleToSearch](https://github.com/AKS-Labs/CircleToSearch) project. Preserve the applicable upstream copyright and license notices when redistributing modified code.

> **Disclaimer:** ScamShield provides risk signals, not guarantees. Never share OTPs, PINs, passwords, or banking credentials with anyone.
