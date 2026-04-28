import logging
from openai import AzureOpenAI
from src.config import settings

logger = logging.getLogger(__name__)


class AzureAIClient:
    """Azure OpenAI client wrapper for the AI service."""
    
    def __init__(self):
        # Use AzureOpenAI client directly
        self.client = AzureOpenAI(
            api_key=settings.azure_openai_api_key,
            api_version=settings.azure_openai_api_version,
            azure_endpoint=settings.azure_openai_endpoint.rstrip('/'),
            azure_deployment=settings.azure_openai_deployment
        )
        self.deployment = settings.azure_openai_deployment
        logger.info(f"Azure OpenAI client initialized with deployment: {self.deployment}")
    
    def chat(self, messages: list, temperature: float = 0.7, stream: bool = False, json_mode: bool = False):
        """
        Send a chat completion request to Azure OpenAI.
        
        Args:
            messages: List of message dicts with 'role' and 'content'
            temperature: Sampling temperature (0-1)
            stream: Whether to stream the response
            json_mode: Whether to return JSON response
            
        Returns:
            Chat completion response
        """
        kwargs = {
            "model": self.deployment,
            "messages": messages,
            "temperature": temperature,
            "stream": stream
        }
        
        if json_mode:
            kwargs["response_format"] = {"type": "json_object"}
        
        return self.client.chat.completions.create(**kwargs)
    
    def chat_with_json_response(self, system_prompt: str, user_message: str):
        """Send a request and parse JSON response."""
        response = self.chat(
            messages=[
                {"role": "system", "content": system_prompt + "\nReturn a valid JSON object."},
                {"role": "user", "content": user_message}
            ],
            json_mode=True
        )
        return response.choices[0].message.content
    
    def transcribe_audio(self, audio_file_path: str):
        """Transcribe audio using Whisper."""
        with open(audio_file_path, "rb") as audio_file:
            transcription = self.client.audio.transcriptions.create(
                model="whisper",
                file=audio_file
            )
        return transcription.text
    
    def text_to_speech(self, text: str, voice: str = "alloy", instructions: str = None):
        """Convert text to speech."""
        kwargs = {
            "model": "tts",
            "voice": voice,
            "input": text
        }
        
        response = self.client.audio.speech.create(**kwargs)
        return response.content
    
    def embed_text(self, texts: list):
        """Get embeddings for texts."""
        response = self.client.embeddings.create(
            model=settings.azure_embedding_deployment,
            input=texts
        )
        return [item.embedding for item in response.data]


# Global client instance
ai_client = AzureAIClient()