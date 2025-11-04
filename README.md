# Azre Speech Functions

Serverless wrappers around Azure AI Speech (Text-to-Speech & Speech-to-Text) built with Azure Functions for Java.  
Use these HTTP endpoints from browsers or backends without exposing your Speech key client-side.
> Tech stack: Java · Maven · Azure Functions (v4) · Azure AI Speech

---

## Features

- **Token relay** – short-lived Speech token for frontend SDKs (no secret in the browser)
- **Text-to-Speech (TTS)** – synthesize audio from text (voice, style, format)
  **Speech-to-Text (STT)** – transcribe audio (file upload or URL)
- **CORS-safe** – locked down origins via env var
- Easy local dev with Functions Core Tools

---

## Quick start

### 1) Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Azure Functions Core Tools v4**
- An **Azure AI Speech** resource  
  Note the **key** and **region** (e.g., `westeurope`).

### 2) Environment variables

Create a `.env` (for local) or set as App Settings (in Azure):

```bash
SPEECH_KEY=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
SPEECH_REGION=westeurope           # e.g. northeurope, westeurope, eastus
CORS_ORIGINS=https://yourapp.dev   # comma-separated list, or * for local only
LOG_LEVEL=INFO                     # optional: TRACE|DEBUG|INFO|WARN|ERROR
```

> Local convenience: Functions Core Tools will read `.env` automatically.

### 3) Run locally

```bash
mvn clean package
# Option A: run via Maven
mvn azure-functions:run
# Option B: run with Core Tools (if you prefer)
func start
```

The Functions host will print your local URLs (typically `http://localhost:7071`).

---

## API

> ⚠️ Replace the function routes below with the actual ones in `src/main/java` if yours differ.  
> (Typical names are shown as examples.)

### Get Speech Token

Exchange the account key for a short-lived token you can use with the browser Speech SDK.

```
GET /api/speech/token
```

**Response**
```json
{
  "region": "westeurope",
  "token": "eyJhbGciOi..."
}
```

**cURL**
```bash
curl http://localhost:7071/api/speech/token
```

### Text-to-Speech (TTS)

Synthesize audio from text. You can choose voice, style and output format.

```
POST /api/speech/tts
Content-Type: application/json
```

**Body**
```json
{
  "text": "Hello world!",
  "voice": "en-US-JennyNeural",
  "format": "audio-16khz-32kbitrate-mono-mp3",
  "style": "general"
}
```

**Response**

- `200 OK` with audio bytes (`Content-Type: audio/mpeg` for mp3, etc.)

**cURL**
```bash
curl -X POST http://localhost:7071/api/speech/tts   -H "Content-Type: application/json"   --data '{"text":"Hello world!","voice":"en-US-JennyNeural","format":"audio-16khz-32kbitrate-mono-mp3"}'   --output speech.mp3
```

### Speech-to-Text (STT)

Upload audio or point to a URL for transcription.

```
POST /api/speech/stt
```

**Option A – upload file (multipart)**
```
Content-Type: multipart/form-data
file=@sample.wav
language=en-US
```

**Option B – JSON with URL**
```json
{
  "audioUrl": "https://example.com/sample.wav",
  "language": "en-US"
}
```

**Response**
```json
{
  "text": "recognized text ...",
  "durationMs": 1234,
  "language": "en-US"
}
```

---

## Security notes

- Never ship your `SPEECH_KEY` to the client; use the **/token** endpoint.
- Limit **CORS** to trusted origins in production.
- Consider **rate limiting** and **auth** (e.g., require a user token before issuing a Speech token).

---

## Troubleshooting

- **401 or 403** from Speech: wrong `SPEECH_KEY` or `SPEECH_REGION`.
- **CORS blocked** in the browser: check `CORS_ORIGINS` matches your site exactly.
- **Audio format errors**: ensure the `format` you request is a valid Speech audio output.
- **Function not found**: verify the route in the Java function annotation matches what you’re calling.

---

## License

MIT (or choose one and add a `LICENSE` file).

---

### Maintainer

**Thor Jørund Nydal** — PRs and issues welcome!
