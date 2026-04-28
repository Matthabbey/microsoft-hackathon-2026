"""
Meeting Intelligence Module
Call transcript analysis, key decisions extraction, summary generation
"""
import json
import logging
from typing import Optional, List
from src.azure_client import ai_client

logger = logging.getLogger(__name__)


def analyze_meeting_transcript(transcript: str, meeting_type: str = "customer_call") -> dict:
    """
    Analyze a meeting or call transcript to extract insights.
    
    Args:
        transcript: The full transcript text
        meeting_type: Type of meeting ("customer_call", "sales", "support", "internal")
        
    Returns:
        Dict with summary, key decisions, action items, participants
    """
    system_prompt = f"""
You are a meeting intelligence engine for enterprise telecom operations.
Analyze the transcript below and return ONLY valid JSON.

For a {meeting_type}, extract:
{{
  "meeting_summary": string (2-3 sentences overview),
  "key_decisions": list of key decisions made,
  "action_items": list of action items with assignee and deadline if mentioned,
  "participants": list of participant names or roles mentioned,
  "topics_discussed": list of main topics,
  "next_steps": string with recommended next steps,
  "sentiment_overall": one of ["positive", "negative", "neutral", "mixed"],
  "follow_up_required": boolean,
  "escalation_needed": boolean
}}
"""
    result = ai_client.chat_with_json_response(system_prompt, transcript)
    
    try:
        # Clean the response - remove any markdown code blocks and whitespace
        cleaned = result.strip()
        if cleaned.startswith("```json"):
            cleaned = cleaned[7:]
        elif cleaned.startswith("```"):
            cleaned = cleaned[3:]
        if cleaned.endswith("```"):
            cleaned = cleaned[:-3]
        cleaned = cleaned.strip()
        
        parsed = json.loads(cleaned)
        logger.info(f"Meeting analyzed: {len(parsed.get('key_decisions', []))} decisions, {len(parsed.get('action_items', []))} action items")
        return parsed
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse meeting analysis: {e}")
        return {
            "meeting_summary": "Analysis failed",
            "key_decisions": [],
            "action_items": [],
            "topics_discussed": [],
            "next_steps": "Manual review required",
            "sentiment_overall": "neutral",
            "follow_up_required": False,
            "escalation_needed": False
        }


def extract_key_decisions(transcript: str) -> List[dict]:
    """
    Extract key decisions from a transcript.
    
    Args:
        transcript: The transcript text
        
    Returns:
        List of decision dicts with decision text and context
    """
    system_prompt = """
You are a decision extraction engine for meeting transcripts.
Extract all key decisions made during the conversation and return ONLY valid JSON.

Return a list of decisions in this format:
{
  "decisions": [
    {
      "decision": string (the decision made),
      "context": string (why this decision was made),
      "participants": list of people involved in the decision
    }
  ]
}
"""
    result = ai_client.chat_with_json_response(system_prompt, transcript)
    
    try:
        # Clean the response - remove any markdown code blocks and whitespace
        cleaned = result.strip()
        if cleaned.startswith("```json"):
            cleaned = cleaned[7:]
        elif cleaned.startswith("```"):
            cleaned = cleaned[3:]
        if cleaned.endswith("```"):
            cleaned = cleaned[:-3]
        cleaned = cleaned.strip()
        
        parsed = json.loads(cleaned)
        return parsed.get("decisions", [])
    except json.JSONDecodeError as e:
        logger.error(f"Failed to extract decisions: {e}")
        return []


def generate_meeting_summary(transcript: str, max_length: int = 200) -> str:
    """
    Generate a concise summary of a meeting transcript.
    
    Args:
        transcript: The full transcript
        max_length: Maximum words in summary
        
    Returns:
        Summary text
    """
    system_prompt = f"""
You are a meeting summarization engine.
Generate a concise summary of the following transcript in no more than {max_length} words.
Focus on the main points, outcomes, and action items.
"""
    response = ai_client.chat(
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": transcript}
        ],
        temperature=0.5
    )
    return response.choices[0].message.content


def extract_action_items(transcript: str) -> List[dict]:
    """
    Extract action items from a transcript.
    
    Args:
        transcript: The transcript text
        
    Returns:
        List of action item dicts
    """
    system_prompt = """
You are an action item extraction engine for meeting transcripts.
Extract all action items and return ONLY valid JSON.

Return this format:
{
  "action_items": [
    {
      "task": string (what needs to be done),
      "assignee": string or null (who is responsible),
      "deadline": string or null (when it's due),
      "priority": one of ["low", "medium", "high"]
    }
  ]
}
"""
    result = ai_client.chat_with_json_response(system_prompt, transcript)
    
    try:
        # Clean the response - remove any markdown code blocks and whitespace
        cleaned = result.strip()
        if cleaned.startswith("```json"):
            cleaned = cleaned[7:]
        elif cleaned.startswith("```"):
            cleaned = cleaned[3:]
        if cleaned.endswith("```"):
            cleaned = cleaned[:-3]
        cleaned = cleaned.strip()
        
        parsed = json.loads(cleaned)
        return parsed.get("action_items", [])
    except json.JSONDecodeError as e:
        logger.error(f"Failed to extract action items: {e}")
        return []


def analyze_call_quality(transcript: str) -> dict:
    """
    Analyze the quality of a customer call.
    
    Args:
        transcript: The call transcript
        
    Returns:
        Dict with quality metrics
    """
    system_prompt = """
You are a call quality analysis engine for customer support calls.
Analyze the transcript and return ONLY valid JSON with quality metrics.

Return this format:
{
  "resolution_status": one of ["resolved", "pending", "escalated", "unresolved"],
  "customer_satisfaction_indicator": number 1-5,
  "agent_effectiveness": one of ["excellent", "good", "average", "poor"],
  "communication_clarity": one of ["excellent", "good", "average", "poor"],
  "first_contact_resolution": boolean,
  "hold_time_mentioned": boolean,
  "transfer_required": boolean,
  "overall_score": number 0-100,
  "improvement_suggestions": list of strings
}
"""
    result = ai_client.chat_with_json_response(system_prompt, transcript)
    
    try:
        # Clean the response - remove any markdown code blocks and whitespace
        cleaned = result.strip()
        if cleaned.startswith("```json"):
            cleaned = cleaned[7:]
        elif cleaned.startswith("```"):
            cleaned = cleaned[3:]
        if cleaned.endswith("```"):
            cleaned = cleaned[:-3]
        cleaned = cleaned.strip()
        
        parsed = json.loads(cleaned)
        logger.info(f"Call quality analyzed: resolution={parsed.get('resolution_status')}, satisfaction={parsed.get('customer_satisfaction_indicator')}")
        return parsed
    except json.JSONDecodeError as e:
        logger.error(f"Failed to analyze call quality: {e}")
        return {
            "resolution_status": "unknown",
            "customer_satisfaction_indicator": 3,
            "overall_score": 50,
            "improvement_suggestions": ["Analysis failed - manual review required"]
        }