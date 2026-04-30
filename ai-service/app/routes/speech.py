from fastapi import APIRouter, UploadFile, File, Query
from fastapi.responses import Response, JSONResponse
from app.services.speech_service import speech_to_text, text_to_speech
from app.services.orchestrator import run_pipeline

router = APIRouter()

@router.post("/tts")
async def tts(
    text: str     = Query(...),
    language: str = Query("en-NG"),
):
    try:
        audio = text_to_speech(text, language)
        return Response(content=audio, media_type="audio/mpeg")
    except Exception as e:
        return JSONResponse(
            status_code=503,
            content={
                "error": "TTS service unavailable",
                "message": "TTS model not deployed yet",
                "text": text
            }
        )

@router.post("/stt")
async def stt(
    file: UploadFile = File(...),
    language: str    = Query("en-NG"),
):
    try:
        audio_bytes = await file.read()
        transcript  = speech_to_text(audio_bytes, language)

        if not transcript:
            return {"error": "Could not transcribe audio", "transcript": ""}

        result = await run_pipeline(transcript=transcript, target_lang="en")
        return {"transcript": transcript, **result}
    except Exception as e:
        return JSONResponse(
            status_code=503,
            content={"error": "STT service unavailable", "message": str(e)}
        )