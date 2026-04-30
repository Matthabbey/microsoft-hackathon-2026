from app.services.openai_service import chat_stream_text

AUTO_RESOLVABLE = {
    "airtime_issue",
    "data_failure",
    "subscription_query",
}

ALWAYS_ESCALATE = {
    "billing_dispute",
}

SUPPORTED_LANGUAGES = {
    "en":  "English",
    "yo":  "Yoruba",
    "ig":  "Igbo",
    "ha":  "Hausa",
    "pcm": "Nigerian Pidgin",
}

def make_decision(intent: str, sentiment: str, urgency: str) -> dict:
    if intent in ALWAYS_ESCALATE:
        return {
            "action":   "escalate",
            "reason":   "Billing disputes require human verification",
            "priority": "high",
        }
    if urgency == "high" and sentiment == "negative":
        return {
            "action":   "escalate",
            "reason":   "High urgency negative complaint — needs immediate human attention",
            "priority": "high",
        }
    if intent in AUTO_RESOLVABLE and urgency in ("low", "medium"):
        return {
            "action":   "auto_resolve",
            "reason":   "Issue is within AI resolution scope",
            "priority": urgency,
        }
    if urgency == "medium":
        return {
            "action":   "notify",
            "reason":   "Handled by AI but flagged for agent follow-up",
            "priority": "medium",
        }
    return {
        "action":   "escalate",
        "reason":   "Issue complexity requires human review",
        "priority": "medium",
    }

def get_escalation_message(intent: str, urgency: str, language: str = "en") -> str:
    lang_name = SUPPORTED_LANGUAGES.get(language, "English")

    lang_instruction = (
        f"Respond ONLY in {lang_name}. Keep phone numbers unchanged. "
        if language != "en"
        else ""
    )

    messages = [
        {"role": "system", "content": (
            "You are Zigi, MTN Nigeria's AI assistant. "
            "The customer's issue needs to be handled by a human agent. "
            "Inform them politely you are transferring them. "
            "Tell them wait time is typically 2-5 minutes. "
            "Be empathetic, brief, and end with reassurance. "
            + lang_instruction
        )},
        {"role": "user", "content": (
            f"Customer issue: {intent}, urgency: {urgency}. "
            "Generate the escalation message."
        )},
    ]
    return chat_stream_text(messages)