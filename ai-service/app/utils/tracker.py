import json
import os
from datetime import datetime

LOG_FILE = "ai_performance_log.jsonl"

def log_interaction(data: dict):
    """
    Appends every pipeline result to a log file.
    Each line is a JSON object — easy to analyze later.
    """
    entry = {
        "timestamp":        datetime.utcnow().isoformat(),
        "intent":           data.get("intent"),
        "sentiment":        data.get("sentiment"),
        "urgency":          data.get("urgency"),
        "decision_action":  data.get("decision", {}).get("action"),
        "decision_reason":  data.get("decision", {}).get("reason"),
        "language":         data.get("language_detected"),
        "transcript_len":   len(data.get("transcript", "")),
        "response_len":     len(data.get("response", "")),
    }
    with open(LOG_FILE, "a") as f:
        f.write(json.dumps(entry) + "\n")