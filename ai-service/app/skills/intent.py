from app.services.openai_service import chat_stream_text

INTENTS = [
    "airtime_issue",
    "billing_dispute",
    "data_failure",
    "network_outage",
    "subscription_query",
    "other",
]

def classify_intent(text: str) -> str:
    messages = [
        {"role": "system", "content": (
            f"You are an MTN Nigeria intent classifier. "
            f"Classify the customer message into exactly one of: "
            f"{', '.join(INTENTS)}. Reply with only the intent label, nothing else."
        )},
        {"role": "user", "content": text},
    ]
    result = chat_stream_text(messages).strip().lower()
    return result if result in INTENTS else "other"