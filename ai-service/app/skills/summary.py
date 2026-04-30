from app.services.openai_service import chat_stream_text

SUPPORTED_LANGUAGES = {
    "en":  "English",
    "yo":  "Yoruba",
    "ig":  "Igbo",
    "ha":  "Hausa",
    "pcm": "Nigerian Pidgin",
}

def summarize(transcript: str, intent: str, language: str = "en") -> str:
    lang_name = SUPPORTED_LANGUAGES.get(language, "English")

    lang_instruction = (
        f"Write the summary in {lang_name}. "
        if language != "en"
        else ""
    )

    messages = [
        {"role": "system", "content": (
            "You are an MTN Nigeria operations analyst. "
            "Given a customer complaint and its intent, produce a short "
            "executive summary (2-3 sentences) covering: the core issue, "
            "any systemic patterns, and recommended action. "
            + lang_instruction
        )},
        {"role": "user", "content": f"Intent: {intent}\nTranscript:\n{transcript}"},
    ]
    return chat_stream_text(messages)