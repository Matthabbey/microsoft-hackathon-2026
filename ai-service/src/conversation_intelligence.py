"""
Conversation Intelligence Module
Real-time transcription + intent extraction + sentiment analysis
"""
import json
import logging
from typing import Optional
from src.azure_client import ai_client

logger = logging.getLogger(__name__)

# Complaint categories for classification
COMPLAINT_CATEGORIES = [
    "Network Quality",
    "Data Consumption", 
    "Failed Transaction",
    "Unauthorized Charge",
    "Roaming",
    "SIM Issue",
    "Agent Conduct",
    "Other"
]

# Priority levels
PRIORITY_LEVELS = ["Low", "Medium", "High"]


def extract_intent(conversation_text: str, use_few_shot: bool = True) -> dict:
    """
    Extract structured intent from customer conversation/complaint.
    
    Uses few-shot prompting to classify into categories and extract
    structured data like customer name, phone number, location, etc.
    
    Args:
        conversation_text: The customer's complaint or conversation text
        use_few_shot: Whether to use few-shot prompting (more accurate)
        
    Returns:
        Dict with category, priority, customer info, summary, suggested_action
    """
    if use_few_shot:
        # Use few-shot prompting like in the notebook
        system_prompt = """You are a complaint classification engine for MTN Nigeria.

Classify the customer complaint below into EXACTLY ONE of these categories:
[Network Quality | Data Consumption | Failed Transaction | Unauthorized Charge | Roaming | SIM Issue | Agent Conduct | Other]

Extract ALL available information and return ONLY valid JSON."""

        user_prompt = f"""Complaint: {conversation_text}

Return JSON with this exact structure:
{{
  "customer_name": string or null,
  "phone_number": string or null,
  "location": string or null,
  "category": one of the categories above,
  "priority": one of ["Low", "Medium", "High"],
  "amount_involved": number or null,
  "summary": string (one sentence max),
  "suggested_action": string
}}"""
        
        result = ai_client.chat_with_json_response(system_prompt, user_prompt)
    else:
        system_prompt = """
You are a complaint parsing engine for MTN Nigeria's CRM system.
Extract structured information from the customer complaint and return ONLY valid JSON.
No preamble, no explanation, no markdown — just the JSON object.

Return this exact structure:
{
  "customer_name": string or null,
  "phone_number": string or null,
  "location": string or null,
  "category": one of ["Network Quality", "Data Consumption", "Failed Transaction",
                      "Unauthorized Charge", "Roaming", "SIM Issue", "Agent Conduct", "Other"],
  "priority": one of ["Low", "Medium", "High"],
  "amount_involved": number or null,
  "summary": string (one sentence max),
  "suggested_action": string
}
"""
        result = ai_client.chat_with_json_response(system_prompt, conversation_text)
    
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
        logger.info(f"Intent extracted: category={parsed.get('category')}, priority={parsed.get('priority')}")
        return parsed
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse intent response: {e}")
        logger.error(f"Raw response was: {repr(result)}")
        return {
            "category": "Other",
            "priority": "Medium",
            "summary": conversation_text[:100],
            "suggested_action": "Manual review required"
        }


def analyze_sentiment(conversation_text: str) -> dict:
    """
    Analyze the sentiment of a customer conversation.
    
    Args:
        conversation_text: The conversation text to analyze
        
    Returns:
        Dict with sentiment label and confidence score
    """
    system_prompt = """
You are a sentiment analysis engine for customer support conversations.
Analyze the sentiment of the customer's message and return ONLY valid JSON.

Return this exact structure:
{
  "sentiment": one of ["positive", "negative", "neutral"],
  "confidence": number between 0 and 1,
  "emotion": one of ["frustrated", "angry", "satisfied", "happy", "neutral", "confused", "worried"],
  "urgency_level": one of ["low", "medium", "high"],
  "key_phrases": list of significant phrases from the conversation
}
"""
    result = ai_client.chat_with_json_response(system_prompt, conversation_text)
    
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
        logger.info(f"Sentiment analyzed: {parsed.get('sentiment')} ({parsed.get('emotion')})")
        return parsed
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse sentiment response: {e}")
        return {
            "sentiment": "neutral",
            "confidence": 0.5,
            "emotion": "neutral",
            "urgency_level": "medium",
            "key_phrases": []
        }


def detect_real_time_intent(text: str) -> dict:
    """
    Real-time intent detection during an ongoing conversation.
    Lightweight version for faster responses.
    
    Args:
        text: Current message from customer
        
    Returns:
        Dict with quick intent classification
    """
    system_prompt = """
You are a real-time intent classifier for customer support.
Classify the customer's message quickly and return ONLY valid JSON.

Return this exact structure:
{
  "intent": one of ["inquiry", "complaint", "request", "question", "feedback", "emergency"],
  "category": one of ["Network Quality", "Data Consumption", "Failed Transaction", 
                      "Unauthorized Charge", "Roaming", "SIM Issue", "Agent Conduct", "Other"],
  "action_required": string describing what action should be taken,
  "quick_response": string with a suggested quick response
}
"""
    result = ai_client.chat_with_json_response(system_prompt, text)
    
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
        return parsed
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse real-time intent: {e}")
        return {
            "intent": "inquiry",
            "category": "Other",
            "action_required": "Manual review",
            "quick_response": "Thank you for contacting us. How can I assist you?"
        }


def generate_response(conversation_history: list, customer_message: str, persona: str = "default") -> str:
    """
    Generate a contextual response based on conversation history.
    
    Args:
        conversation_history: List of previous messages
        customer_message: Current customer message
        persona: Response persona ("default", "friendly", "formal")
        
    Returns:
        Generated response text
    """
    personas = {
        "default": "You are a helpful MTN Nigeria customer support assistant.",
        "friendly": """You are Zigi, MTN Nigeria's friendly customer support assistant.
You speak in a warm, casual Nigerian tone. Use mild Pidgin where appropriate.
End with: "No worry, we dey here for you!" """,
        "formal": """You are a professional MTN Nigeria customer support representative.
Be formal, concise, and helpful. Use proper English.
End with: "Is there anything else I can help you with?" """
    }
    
    system_prompt = personas.get(persona, personas["default"])
    
    messages = [{"role": "system", "content": system_prompt}]
    
    # Add conversation history (last 5 messages)
    for msg in conversation_history[-5:]:
        messages.append(msg)
    
    messages.append({"role": "user", "content": customer_message})
    
    response = ai_client.chat(messages=messages, temperature=0.7)
    return response.choices[0].message.content