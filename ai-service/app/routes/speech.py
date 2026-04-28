from fastapi import APIRouter, UploadFile, File, Query
from fastapi.responses import Response
from app.services.speech_service import speech_to_text, text_to_speech
from app.services.orchestrator import run_pipeline

router = APIRouter()

@router.post("/tts")
async def tts(
    text: str     = Query(...),
    language: str = Query("en-NG"),
):
    """Convert text to speech. Returns MP3 audio."""
    audio = text_to_speech(text, language)
    return Response(content=audio, media_type="audio/mpeg")

@router.post("/stt")
async def stt(
    file: UploadFile = File(...),
    language: str    = Query("en-NG"),
):
    """Upload audio, get transcript + full AI analysis in detected language."""
    audio_bytes = await file.read()
    transcript  = speech_to_text(audio_bytes, language)

    if not transcript:
        return {"error": "Could not transcribe audio", "transcript": ""}

    # Pass "en" so orchestrator auto-detects from transcript
    result = await run_pipeline(transcript=transcript, target_lang="en")
    return {"transcript": transcript, **result}