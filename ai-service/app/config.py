from dotenv import load_dotenv
import os
from pathlib import Path

# Explicitly find .env relative to this file
load_dotenv(dotenv_path=Path(__file__).resolve().parent.parent / ".env")

AZURE_COGNITIVE_ENDPOINT        = os.getenv("AZURE_COGNITIVE_ENDPOINT")
GPT_5_4_NANO_KEY                = os.getenv("GPT_5_4_NANO_KEY")
OPENAI_BASE_URL                 = f"{AZURE_COGNITIVE_ENDPOINT}/openai/v1/"
CHAT_DEPLOYMENT                 = "gpt-5.4-nano"

AZURE_COGNITIVE_ENDPOINT_SWEDEN = os.getenv("AZURE_COGNITIVE_ENDPOINT_SWEDEN")
AZURE_OPENAI_SECRET_SWEDEN      = os.getenv("AZURE_OPENAI_SECRET_SWEDEN")

AZURE_FOUNDRY_API_KEY           = os.getenv("AZURE_FOUNDRY_API_KEY")
AZURE_OPENAI_ENDPOINT           = os.getenv("AZURE_OPENAI_ENDPOINT")
AZURE_OPENAI_KEY                = os.getenv("AZURE_OPENAI_KEY")