# MTN Zigi AI Service

AI-powered customer support backend for MTN Nigeria.
Built with FastAPI + Azure OpenAI as part of the Microsoft Hackathon 2026.

## What It Does

- Receives customer complaints (text or audio)
- Detects language automatically (English, Yoruba, Igbo, Hausa, Nigerian Pidgin)
- Classifies intent, sentiment, and urgency
- Decides whether to auto-resolve or escalate to a human agent
- Responds in the same language the customer used
- Converts responses to speech (TTS)
- Transcribes audio complaints (STT)
- Tracks AI performance metrics
- Detects systemic patterns across batch complaints

## Requirements

- Python 3.10+
- Azure Account with the following resources:
  - Azure AI multi-service resource (East US)
  - Azure AI Foundry resource (Sweden Central) for TTS

## Setup

### 1. Clone the repo

```bash
git clone https://github.com/Matthabbey/microsoft-hackathon-2026.git
cd microsoft-hackathon-2026/ai-service
```

### 2. Create a virtual environment

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# Mac/Linux
python3 -m venv venv
source venv/bin/activate
```

### 3. Install dependencies

```bash
pip install -r requirements.txt
```

### 4. Create your `.env` file

Create a `.env` file in the `ai-service/` folder:
Never commit your `.env` file. It is already in `.gitignore`.

### 5. Run the server

```bash
python -m uvicorn app.main:app --reload --port 8000
```

> Never commit your `.env` file. It is already in `.gitignore`.

### 5. Run the server

```bash
python -m uvicorn app.main:app --reload --port 8000
```

### 6. Open the API docs
http://127.0.0.1:8000/docs

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Service status |
| GET | `/health` | Health check |
| GET | `/metrics` | AI performance metrics |
| POST | `/analyze/text` | Analyze a single complaint |
| POST | `/analyze/batch` | Analyze multiple complaints + pattern insight |
| POST | `/speech/tts` | Convert text to speech (MP3) |
| POST | `/speech/stt` | Upload audio, get transcript + analysis |

## Languages Supported

| Code | Language |
|------|----------|
| `en` | English |
| `yo` | Yoruba |
| `ig` | Igbo |
| `ha` | Hausa |
| `pcm` | Nigerian Pidgin |

Language is auto-detected — no need to specify it manually.

## Example Request

`POST /analyze/text`

```json
{
  "transcript": "Oga abeg my data don finish and MTN dey deduct my airtime",
  "language": "en"
}
```

Example response:

```json
{
  "transcript": "My data has finished and MTN is deducting my airtime",
  "intent": "data_failure",
  "sentiment": "negative",
  "urgency": "medium",
  "decision": {
    "action": "auto_resolve",
    "reason": "Issue is within AI resolution scope",
    "priority": "medium"
  },
  "response": "Oga no worry! Dial *556# to check your data balance...",
  "insight": "Customer experiencing data depletion...",
  "language_detected": "pcm"
}
```
## Built With

- [FastAPI](https://fastapi.tiangolo.com/)
- [Azure OpenAI](https://azure.microsoft.com/en-us/products/ai-services/openai-service)
- [Azure AI Foundry](https://azure.microsoft.com/en-us/products/ai-foundry)
- [Azure Cognitive Services](https://azure.microsoft.com/en-us/products/cognitive-services)

## Team

Team 10 — Microsoft Hackathon 2026