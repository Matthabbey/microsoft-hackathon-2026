from openai import OpenAI
from app.config import OPENAI_BASE_URL, GPT_5_4_NANO_KEY, CHAT_DEPLOYMENT

openai_client = OpenAI(
    base_url=OPENAI_BASE_URL,
    api_key=GPT_5_4_NANO_KEY,
)

def chat(messages: list[dict], stream: bool = False):
    return openai_client.chat.completions.create(
        model=CHAT_DEPLOYMENT,
        messages=messages,
        stream=stream,
    )

def chat_stream_text(messages: list[dict]) -> str:
    stream = chat(messages, stream=True)
    result = ""
    for chunk in stream:
        if not chunk.choices:
            continue
        delta = chunk.choices[0].delta.content
        if delta:
            result += delta
    return result