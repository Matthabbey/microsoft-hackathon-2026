from pydantic import BaseModel
from typing import Optional, List

class AnalyzeRequest(BaseModel):
    transcript: str
    language: Optional[str] = "en"

class BatchAnalyzeRequest(BaseModel):
    transcripts: List[str]