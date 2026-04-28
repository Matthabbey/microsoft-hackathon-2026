"""
AI Communication Intelligence Service
FastAPI application with endpoints for conversation, meeting, and follow-up intelligence
"""
import logging
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List

from src.config import settings
from src.conversation_intelligence import (
    extract_intent,
    analyze_sentiment,
    detect_real_time_intent,
    generate_response
)
from src.meeting_intelligence import (
    analyze_meeting_transcript,
    extract_key_decisions,
    generate_meeting_summary,
    extract_action_items,
    analyze_call_quality
)
from src.followup_generator import (
    generate_sms_followup,
    generate_whatsapp_followup,
    generate_email_followup,
    generate_followup_for_channel,
    generate_auto_response,
    generate_resolution_summary
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s:%(lineno)d - %(message)s"
)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="Telecom AI Communication Intelligence",
    description="AI-powered conversation intelligence, meeting analysis, and follow-up generation",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================================================
# Request/Response Models
# ============================================================================

class IntentRequest(BaseModel):
    text: str
    use_few_shot: bool = True


class SentimentRequest(BaseModel):
    text: str


class RealTimeIntentRequest(BaseModel):
    text: str


class GenerateResponseRequest(BaseModel):
    conversation_history: List[dict]
    customer_message: str
    persona: str = "default"


class MeetingTranscriptRequest(BaseModel):
    transcript: str
    meeting_type: str = "customer_call"


class KeyDecisionsRequest(BaseModel):
    transcript: str


class MeetingSummaryRequest(BaseModel):
    transcript: str
    max_length: int = 200


class ActionItemsRequest(BaseModel):
    transcript: str


class CallQualityRequest(BaseModel):
    transcript: str


class SMSFollowupRequest(BaseModel):
    summary: str
    customer_name: Optional[str] = None


class WhatsAppFollowupRequest(BaseModel):
    summary: str
    customer_name: Optional[str] = None


class EmailFollowupRequest(BaseModel):
    summary: str
    customer_name: Optional[str] = None
    customer_email: Optional[str] = None
    issue_category: str = "General"


class FollowupChannelRequest(BaseModel):
    conversation_data: dict
    channel: str = "sms"


class AutoResponseRequest(BaseModel):
    customer_message: str
    intent: Optional[dict] = None


class ResolutionSummaryRequest(BaseModel):
    complaint: str
    resolution: str


# ============================================================================
# Health Check
# ============================================================================

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "telecom-ai-communication-intelligence",
        "version": "1.0.0"
    }


# ============================================================================
# Conversation Intelligence Endpoints
# ============================================================================

@app.post("/api/ai/conversation/intent")
async def conversation_intent_extraction(request: IntentRequest):
    """Extract structured intent from customer conversation/complaint."""
    try:
        result = extract_intent(request.text, request.use_few_shot)
        return {"success": True, "data": result}
    except Exception as e:
        import traceback
        logger.error(f"Intent extraction error: {e}\n{traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/conversation/sentiment")
async def conversation_sentiment_analysis(request: SentimentRequest):
    """Analyze sentiment of customer conversation."""
    try:
        result = analyze_sentiment(request.text)
        return {"success": True, "data": result}
    except Exception as e:
        logger.error(f"Sentiment analysis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/conversation/real-time-intent")
async def real_time_intent_detection(request: RealTimeIntentRequest):
    """Real-time intent detection during ongoing conversation."""
    try:
        result = detect_real_time_intent(request.text)
        return {"success": True, "data": result}
    except Exception as e:
        logger.error(f"Real-time intent error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/conversation/response")
async def generate_conversation_response(request: GenerateResponseRequest):
    """Generate contextual response based on conversation history."""
    try:
        result = generate_response(
            request.conversation_history,
            request.customer_message,
            request.persona
        )
        return {"success": True, "data": {"response": result}}
    except Exception as e:
        logger.error(f"Response generation error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ============================================================================
# Meeting Intelligence Endpoints
# ============================================================================

@app.post("/api/ai/meeting/analyze")
async def meeting_analysis(request: MeetingTranscriptRequest):
    """Analyze meeting/call transcript for insights."""
    try:
        result = analyze_meeting_transcript(request.transcript, request.meeting_type)
        return {"success": True, "data": result}
    except Exception as e:
        logger.error(f"Meeting analysis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/meeting/decisions")
async def extract_decisions(request: KeyDecisionsRequest):
    """Extract key decisions from transcript."""
    try:
        result = extract_key_decisions(request.transcript)
        return {"success": True, "data": {"decisions": result}}
    except Exception as e:
        logger.error(f"Decision extraction error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/meeting/summary")
async def meeting_summary(request: MeetingSummaryRequest):
    """Generate concise meeting summary."""
    try:
        result = generate_meeting_summary(request.transcript, request.max_length)
        return {"success": True, "data": {"summary": result}}
    except Exception as e:
        logger.error(f"Summary generation error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/meeting/action-items")
async def meeting_action_items(request: ActionItemsRequest):
    """Extract action items from transcript."""
    try:
        result = extract_action_items(request.transcript)
        return {"success": True, "data": {"action_items": result}}
    except Exception as e:
        logger.error(f"Action items extraction error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/meeting/call-quality")
async def call_quality_analysis(request: CallQualityRequest):
    """Analyze quality of customer call."""
    try:
        result = analyze_call_quality(request.transcript)
        return {"success": True, "data": result}
    except Exception as e:
        logger.error(f"Call quality analysis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ============================================================================
# Follow-up Generation Endpoints
# ============================================================================

@app.post("/api/ai/followup/sms")
async def sms_followup(request: SMSFollowupRequest):
    """Generate SMS follow-up message."""
    try:
        result = generate_sms_followup(request.summary, request.customer_name)
        return {"success": True, "data": {"message": result}}
    except Exception as e:
        logger.error(f"SMS follow-up error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/followup/whatsapp")
async def whatsapp_followup(request: WhatsAppFollowupRequest):
    """Generate WhatsApp follow-up message."""
    try:
        result = generate_whatsapp_followup(request.summary, request.customer_name)
        return {"success": True, "data": {"message": result}}
    except Exception as e:
        logger.error(f"WhatsApp follow-up error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/followup/email")
async def email_followup(request: EmailFollowupRequest):
    """Generate email follow-up message."""
    try:
        result = generate_email_followup(
            request.summary,
            request.customer_name,
            request.customer_email,
            request.issue_category
        )
        return {"success": True, "data": result}
    except Exception as e:
        logger.error(f"Email follow-up error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/followup/channel")
async def followup_by_channel(request: FollowupChannelRequest):
    """Generate follow-up for specific channel (sms/whatsapp/email)."""
    try:
        result = generate_followup_for_channel(
            request.conversation_data,
            request.channel
        )
        return {"success": True, "data": {"message": result, "channel": request.channel}}
    except Exception as e:
        logger.error(f"Channel follow-up error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/followup/auto-response")
async def auto_response(request: AutoResponseRequest):
    """Generate automatic response based on customer message."""
    try:
        result = generate_auto_response(request.customer_message, request.intent)
        return {"success": True, "data": {"response": result}}
    except Exception as e:
        logger.error(f"Auto response error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/followup/resolution-summary")
async def resolution_summary(request: ResolutionSummaryRequest):
    """Generate resolution summary for customer records."""
    try:
        result = generate_resolution_summary(request.complaint, request.resolution)
        return {"success": True, "data": {"summary": result}}
    except Exception as e:
        logger.error(f"Resolution summary error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ============================================================================
# Main Entry Point
# ============================================================================

if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting AI Communication Intelligence Service on {settings.host}:{settings.port}")
    uvicorn.run(app, host=settings.host, port=settings.port)