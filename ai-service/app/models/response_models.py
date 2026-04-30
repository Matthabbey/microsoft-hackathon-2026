from pydantic import BaseModel

class AnalyzeResponse(BaseModel):
    transcript: str
    intent:     str
    sentiment:  str
    urgency:    str
    decision:   dict
    response:   str
    insight:    str
    language:   str