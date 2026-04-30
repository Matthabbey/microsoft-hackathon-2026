from app.services.openai_service import chat_stream_text

SUPPORTED_LANGUAGES = {
    "en":  "English",
    "yo":  "Yoruba",
    "ig":  "Igbo",
    "ha":  "Hausa",
    "pcm": "Nigerian Pidgin",
}

MTN_SYSTEM_PROMPT = """
You are Zigi, MTN Nigeria's intelligent customer support assistant.
Respond in a friendly, concise, helpful manner.
Always include relevant USSD codes when available.

Common MTN Nigeria codes:
  *131#        — check airtime balance
  *556#        — check data balance
  *131*7#      — gift data to another MTN line
  *123#        — buy airtime or data bundle
  *587#        — manage/cancel unwanted subscriptions
  *460*1#      — report network issues
  300 or 180   — call MTN customer care (free)
  WhatsApp     — 09033000001

If you cannot resolve the issue, direct the customer to call 300
or WhatsApp 09033000001.
End every response with: "Is there anything else I can help you with?"
"""

def generate_response(transcript: str, context: dict) -> str:
    intent   = context.get("intent",   "other")
    urgency  = context.get("urgency",  "low")
    language = context.get("language", "en")

    lang_name = SUPPORTED_LANGUAGES.get(language, "English")

    lang_instruction = (
        f"IMPORTANT: Respond ONLY in {lang_name}. "
        "Keep all USSD codes and phone numbers unchanged. "
        if language != "en"
        else ""
    )

    messages = [
        {"role": "system", "content": MTN_SYSTEM_PROMPT + "\n" + lang_instruction},
        {"role": "user",   "content": (
            f"Customer complaint (intent={intent}, urgency={urgency}):\n{transcript}"
        )},
    ]
    return chat_stream_text(messages)