from openai import AzureOpenAI
from app.config import (
    AZURE_COGNITIVE_ENDPOINT_SWEDEN,
    AZURE_OPENAI_SECRET_SWEDEN,
)
import os
import tempfile

TTS_DEPLOYMENT     = os.getenv("AZURE_TTS_DEPLOYMENT", "tts")
WHISPER_DEPLOYMENT = os.getenv("AZURE_WHISPER_DEPLOYMENT", "whisper")

tts_client = AzureOpenAI(
    azure_endpoint=AZURE_COGNITIVE_ENDPOINT_SWEDEN,
    api_key=AZURE_OPENAI_SECRET_SWEDEN,
    api_version="2025-03-01-preview"
)

def text_to_speech(text: str, language: str = "en-NG") -> bytes:
    with tts_client.audio.speech.with_streaming_response.create(
        model=TTS_DEPLOYMENT,
        voice="alloy",
        input=text,
    ) as response:
        audio_bytes = b""
        for chunk in response.iter_bytes():
            audio_bytes += chunk
    return audio_bytes

def speech_to_text(audio_bytes: bytes, language: str = "en-NG") -> str:
    try:
        with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as tmp:
            tmp.write(audio_bytes)
            tmp_path = tmp.name

        with open(tmp_path, "rb") as audio_file:
            transcription = tts_client.audio.transcriptions.create(
                model=WHISPER_DEPLOYMENT,
                file=audio_file,
            )
        return transcription.text
    except Exception as e:
        return ""