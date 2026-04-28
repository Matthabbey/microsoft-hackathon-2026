from app.services.openai_service import chat_stream_text
from app.skills.intent import classify_intent
from app.skills.summary import summarize
from app.skills.response import generate_response
from app.skills.language import detect_language, translate_response
from app.skills.decision import make_decision, get_escalation_message
import json
import re

async def run_pipeline(transcript: str, target_lang: str = "en") -> dict:
    # Step 1 — Cleanup
    clean = _cleanup(transcript)

    # Step 2 — Auto detect language
    if target_lang in ("en", "en-NG"):
        detected = detect_language(clean)
    else:
        detected = target_lang.split("-")[0]

    # Step 3 — Intent
    intent = classify_intent(clean)

    # Step 4 — Sentiment + urgency
    sentiment, urgency = _sentiment_urgency(clean)

    # Step 5 — Decision
    decision = make_decision(intent, sentiment, urgency)

    # Step 6 — Generate response in detected language
    context = {
        "intent":   intent,
        "urgency":  urgency,
        "language": detected,
    }

    if decision["action"] == "escalate":
        response = get_escalation_message(intent, urgency, detected)
    else:
        response = generate_response(clean, context)

    # Step 7 — Summary in detected language
    insight = summarize(clean, intent, detected)

    return {
        "transcript":        clean,
        "intent":            intent,
        "sentiment":         sentiment,
        "urgency":           urgency,
        "decision":          decision,
        "response":          response,
        "insight":           insight,
        "language_detected": detected,
        "language":          detected,
    }

def _cleanup(text: str) -> str:
    messages = [
        {"role": "system", "content": (
            "You are a telecom transcript cleaner for MTN Nigeria. "
            "Normalize Pidgin English, fix speech-to-text errors, "
            "expand USSD shorthand. Return only the cleaned transcript."
        )},
        {"role": "user", "content": text},
    ]
    return chat_stream_text(messages)

def _sentiment_urgency(text: str) -> tuple[str, str]:
    messages = [
        {"role": "system", "content": (
            "Analyze the customer message. "
            "Reply JSON only with no markdown: "
            "{\"sentiment\": \"positive|neutral|negative\", "
            "\"urgency\": \"low|medium|high\"}"
        )},
        {"role": "user", "content": text},
    ]
    raw = chat_stream_text(messages)
    raw = re.sub(r"```json|```", "", raw).strip()
    try:
        data = json.loads(raw)
        return data.get("sentiment", "neutral"), data.get("urgency", "low")
    except Exception:
        return "neutral", "low"