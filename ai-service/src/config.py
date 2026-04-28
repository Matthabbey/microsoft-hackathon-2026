from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # Azure OpenAI
    azure_openai_endpoint: str
    azure_openai_api_key: str
    azure_openai_deployment: str
    azure_openai_api_version: str 
    
    # Azure Cognitive Services
    azure_cognitive_endpoint: str
    azure_cognitive_endpoint_sweden: str
    
    # Azure Foundry
    azure_foundry_api_key: str
    
    # Embedding model
    azure_embedding_deployment: str
    azure_embedding_dimensions: int
    
    # Server
    host: str = "0.0.0.0"
    port: int = 8000

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()