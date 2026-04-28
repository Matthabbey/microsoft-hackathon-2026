from openai import AzureOpenAI
import azure.cognitiveservices.speech as speechsdk
from app.config import (
    AZURE_COGNITIVE_ENDPOINT_SWEDEN,
    AZURE_OPENAI_SECRET_SWEDEN,
    AZURE_COGNITIVE_ENDPOINT,
    GPT_5_4_NANO_KEY,
)

tts_client = AzureOpenAI(
    azure_endpoint=AZURE_COGNITIVE_ENDPOINT_SWEDEN,
    api_key=AZURE_OPENAI_SECRET_SWEDEN,
    api_version="2025-03-01-preview"
)

stt_config = speechsdk.SpeechConfig(
    subscription=GPT_5_4_NANO_KEY,
    endpoint=AZURE_COGNITIVE_ENDPOINT
)

def text_to_speech(text: str, language: str = "en-NG") -> bytes:
    with tts_client.audio.speech.with_streaming_response.create(
        model="tts",
        voice="alloy",
        input=text,
    ) as response:
        audio_bytes = b""
        for chunk in response.iter_bytes():
            audio_bytes += chunk
    return audio_bytes

def speech_to_text(audio_bytes: bytes, language: str = "en-NG") -> str:
    stream = speechsdk.audio.PushAudioInputStream()
    stream.write(audio_bytes)
    stream.close()

    audio_cfg  = speechsdk.audio.AudioConfig(stream=stream)
    recognizer = speechsdk.SpeechRecognizer(
        speech_config=stt_config,
        audio_config=audio_cfg,
    )
    result = recognizer.recognize_once()

    if result.reason == speechsdk.ResultReason.RecognizedSpeech:
        return result.text
    return ""