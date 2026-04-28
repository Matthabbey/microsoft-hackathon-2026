"""
Follow-up Generator Module
Auto-generate follow-ups from conversations (SMS/WhatsApp/Email)
"""
import json
import logging
from typing import Optional, List
from src.azure_client import ai_client

logger = logging.getLogger(__name__)


def generate_sms_followup(conversation_summary: str, customer_name: Optional[str] = None) -> str:
    """
    Generate an SMS follow-up message from a conversation.
    
    SMS should be concise (160 chars max ideally).
    
    Args:
        conversation_summary: Summary of the conversation
        customer_name: Optional customer name for personalization
        
    Returns:
        SMS message text
    """
    system_prompt = """
You are a follow-up message generator for MTN Nigeria customer support.
Generate a concise SMS follow-up message.

Rules:
- Keep it under 160 characters
- Be friendly and professional
- Include a call to action if needed
- Never include sensitive information like full account numbers

Return ONLY the message text, no JSON.
"""
    user_msg = f"Conversation summary: {conversation_summary}"
    if customer_name:
        user_msg += f"\nCustomer name: {customer_name}"
    
    response = ai_client.chat(
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_msg}
        ],
        temperature=0.5
    )
    
    message = response.choices[0].message.content.strip()
    logger.info(f"SMS follow-up generated: {len(message)} chars")
    return message


def generate_whatsapp_followup(conversation_summary: str, customer_name: Optional[str] = None) -> str:
    """
    Generate a WhatsApp follow-up message from a conversation.
    
    WhatsApp allows more text and supports formatting.
    
    Args:
        conversation_summary: Summary of the conversation
        customer_name: Optional customer name
        
    Returns:
        WhatsApp message text (can include basic formatting)
    """
    system_prompt = """
You are a WhatsApp follow-up message generator for MTN Nigeria.
Generate a friendly WhatsApp follow-up message.

Rules:
- Can be longer than SMS (up to 4096 chars)
- Use emojis appropriately
- Be conversational and friendly
- Include relevant links or USSD codes if applicable
- Format with *bold* for important info

Return ONLY the message text, no JSON.
"""
    user_msg = f"Conversation summary: {conversation_summary}"
    if customer_name:
        user_msg += f"\nCustomer name: {customer_name}"
    
    response = ai_client.chat(
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_msg}
        ],
        temperature=0.6
    )
    
    message = response.choices[0].message.content.strip()
    logger.info(f"WhatsApp follow-up generated: {len(message)} chars")
    return message


def generate_email_followup(conversation_summary: str, customer_name: Optional[str] = None, 
                           customer_email: Optional[str] = None, issue_category: str = "General") -> dict:
    """
    Generate a formal email follow-up from a conversation.
    
    Args:
        conversation_summary: Summary of the conversation
        customer_name: Optional customer name
        customer_email: Optional customer email
        issue_category: Category of the issue (for subject line)
        
    Returns:
        Dict with subject, body, and other email fields
    """
    system_prompt = """
You are an email follow-up generator for MTN Nigeria customer support.
Generate a professional follow-up email and return ONLY valid JSON.

Return this format:
{
  "subject": string (email subject line),
  "body": string (email body - can include HTML formatting),
  "signature": string (email signature),
  "priority": one of ["low", "normal", "high"],
  "attachments": list of strings (if any attachments needed)
}
"""
    user_msg = f"Conversation summary: {conversation_summary}\nIssue category: {issue_category}"
    if customer_name:
        user_msg += f"\nCustomer name: {customer_name}"
    if customer_email:
        user_msg += f"\nCustomer email: {customer_email}"
    
    result = ai_client.chat_with_json_response(system_prompt, user_msg)
    
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
        logger.info(f"Email follow-up generated for category: {issue_category}")
        return parsed
    except json.JSONDecodeError as e:
        logger.error(f"Failed to generate email: {e}")
        return {
            "subject": f"Follow-up: {issue_category} - MTN Nigeria",
            "body": f"Dear Customer,\n\nThank you for contacting MTN Nigeria.\n\n{conversation_summary}\n\nPlease don't hesitate to reach out if you need further assistance.\n\nBest regards,\nMTN Customer Support",
            "signature": "MTN Nigeria Customer Support",
            "priority": "normal",
            "attachments": []
        }


def generate_followup_for_channel(conversation_data: dict, channel: str = "sms") -> str:
    """
    Generate a follow-up message for a specific channel.
    
    Args:
        conversation_data: Dict with conversation details
        channel: One of "sms", "whatsapp", "email"
        
    Returns:
        Generated message for the channel
    """
    summary = conversation_data.get("summary", "")
    customer_name = conversation_data.get("customer_name")
    customer_email = conversation_data.get("customer_email")
    category = conversation_data.get("category", "General")
    
    if channel == "sms":
        return generate_sms_followup(summary, customer_name)
    elif channel == "whatsapp":
        return generate_whatsapp_followup(summary, customer_name)
    elif channel == "email":
        email = generate_email_followup(summary, customer_name, customer_email, category)
        return f"Subject: {email['subject']}\n\n{email['body']}\n\n{email['signature']}"
    else:
        return "Invalid channel specified"


def generate_auto_response(customer_message: str, intent: dict = None) -> str:
    """
    Generate an automatic response based on customer message and detected intent.
    
    Args:
        customer_message: The customer's message
        intent: Optional intent data from conversation_intelligence
        
    Returns:
        Auto-generated response
    """
    system_prompt = """
You are an auto-response generator for MTN Nigeria customer support.
Generate a quick, helpful response to the customer's message.

Rules:
- Be concise but friendly
- If you need more info, ask a clarifying question
- Include relevant USSD codes if applicable
- End with an offer to help further

Return ONLY the response text, no JSON.
"""
    
    user_msg = customer_message
    if intent and intent.get("category"):
        user_msg += f"\n\nDetected category: {intent['category']}"
    
    response = ai_client.chat(
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_msg}
        ],
        temperature=0.6
    )
    
    return response.choices[0].message.content


def generate_resolution_summary(complaint: str, resolution: str) -> str:
    """
    Generate a resolution summary for customer records.
    
    Args:
        complaint: Original customer complaint
        resolution: How the issue was resolved
        
    Returns:
        Resolution summary text
    """
    system_prompt = """
You are a resolution summary generator for customer support.
Create a concise summary of how a complaint was resolved.

Return ONLY the summary text (2-3 sentences), no JSON.
"""
    
    response = ai_client.chat(
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": f"Complaint: {complaint}\n\nResolution: {resolution}"}
        ],
        temperature=0.5
    )
    
    return response.choices[0].message.content