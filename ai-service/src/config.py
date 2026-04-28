from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # Azure OpenAI
    azure_openai_endpoint: str = "https://telecom-hackathon.openai.azure.com/"
    azure_openai_api_key: str = ""
    azure_openai_deployment: str = "gpt-5.4-nano"
    azure_openai_api_version: str = "2024-06-01"
    
    # Azure Cognitive Services
    azure_cognitive_endpoint: str = "https://osina-mnndoh9y-eastus2.cognitiveservices.azure.com"
    azure_cognitive_endpoint_sweden: str = "https://osina-mo9bbdw6-swedencentral.cognitiveservices.azure.com"
    
    # Azure Foundry
    azure_foundry_api_key: str = ""
    
    # Embedding model
    azure_embedding_deployment: str = "text-embedding-3-small"
    azure_embedding_dimensions: int = 1536
    
    # Server
    host: str = "0.0.0.0"
    port: int = 8000

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()