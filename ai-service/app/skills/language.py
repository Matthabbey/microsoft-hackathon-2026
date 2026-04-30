from app.services.openai_service import chat_stream_text

SUPPORTED = {
    "en":  "English",
    "yo":  "Yoruba",
    "ig":  "Igbo",
    "ha":  "Hausa",
    "pcm": "Nigerian Pidgin",
}

PIDGIN_MARKERS = [
    "don", "dey", "oga", "abeg", "wahala", "wetin",
    "na", "sabi", "chop", "dem", "una", "comot"
]

def detect_language(text: str) -> str:
    """Use AI to detect language including Nigerian Pidgin."""
    # Quick Pidgin check first
    text_lower = text.lower()
    pidgin_hits = sum(1 for word in PIDGIN_MARKERS if word in text_lower.split())
    if pidgin_hits >= 2:
        return "pcm"

    messages = [
        {"role": "system", "content": (
            "Detect the language of the text. "
            "Reply with ONLY one of these codes, nothing else: "
            "en, yo, ig, ha, pcm "
            "(en=English, yo=Yoruba, ig=Igbo, ha=Hausa, pcm=Nigerian Pidgin). "
            "If unsure default to en."
        )},
        {"role": "user", "content": text},
    ]
    result = chat_stream_text(messages).strip().lower()
    return result if result in SUPPORTED else "en"


def translate_response(text: str, target_lang: str) -> str:
    """Translate Zigi's response into the detected language."""
    if target_lang not in SUPPORTED or target_lang == "en":
        return text

    lang_name = SUPPORTED[target_lang]
    messages = [
        {"role": "system", "content": (
            f"You are translating an MTN Nigeria customer support response into {lang_name}. "
            "Keep USSD codes like *556# and phone numbers like 09033000001 unchanged. "
            "Keep the friendly, helpful tone. "
            f"Respond ONLY in {lang_name}."
        )},
        {"role": "user", "content": text},
    ]
    return chat_stream_text(messages)