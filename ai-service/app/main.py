from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes.analyze import router as analyze_router
from app.routes.speech import router as speech_router
import json
import os

app = FastAPI(title="MTN Zigi AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(analyze_router, prefix="/analyze", tags=["Analyze"])
app.include_router(speech_router, prefix="/speech", tags=["Speech"])

@app.get("/")
def root():
    return {"message": "MTN Zigi AI Service is running", "docs": "/docs"}

@app.get("/health")
def health():
    return {"status": "ok", "service": "MTN Zigi AI"}

@app.get("/metrics")
def metrics():
    """Returns AI performance summary from interaction logs."""
    log_file = "ai_performance_log.jsonl"
    if not os.path.exists(log_file):
        return {"message": "No interactions logged yet"}

    entries = []
    with open(log_file) as f:
        for line in f:
            try:
                entries.append(json.loads(line))
            except Exception:
                continue

    if not entries:
        return {"message": "No interactions logged yet"}

    total = len(entries)

    # Intent breakdown
    intents = {}
    for e in entries:
        i = e.get("intent", "unknown")
        intents[i] = intents.get(i, 0) + 1

    # Decision breakdown
    decisions = {}
    for e in entries:
        d = e.get("decision_action", "unknown")
        decisions[d] = decisions.get(d, 0) + 1

    # Sentiment breakdown
    sentiments = {}
    for e in entries:
        s = e.get("sentiment", "unknown")
        sentiments[s] = sentiments.get(s, 0) + 1

    # Language breakdown
    languages = {}
    for e in entries:
        l = e.get("language", "unknown")
        languages[l] = languages.get(l, 0) + 1

    # Urgency breakdown
    urgencies = {}
    for e in entries:
        u = e.get("urgency", "unknown")
        urgencies[u] = urgencies.get(u, 0) + 1

    # Escalation rate
    escalations = decisions.get("escalate", 0)
    escalation_rate = round((escalations / total) * 100, 1)

    # Auto resolve rate
    auto_resolved = decisions.get("auto_resolve", 0)
    auto_resolve_rate = round((auto_resolved / total) * 100, 1)

    return {
        "total_interactions":  total,
        "escalation_rate":     f"{escalation_rate}%",
        "auto_resolve_rate":   f"{auto_resolve_rate}%",
        "intent_breakdown":    intents,
        "decision_breakdown":  decisions,
        "sentiment_breakdown": sentiments,
        "language_breakdown":  languages,
        "urgency_breakdown":   urgencies,
        "last_interaction":    entries[-1]["timestamp"],
    }