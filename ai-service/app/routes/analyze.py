from fastapi import APIRouter
from app.models.request_models import AnalyzeRequest, BatchAnalyzeRequest
from app.services.orchestrator import run_pipeline
from app.utils.tracker import log_interaction
from app.services.openai_service import chat_stream_text

router = APIRouter()

@router.post("/text")
async def analyze_text(body: AnalyzeRequest):
    result = await run_pipeline(
        transcript=body.transcript,
        target_lang=body.language or "en",
    )
    log_interaction(result)
    return result


@router.post("/batch")
async def analyze_batch(body: BatchAnalyzeRequest):
    """
    Analyze multiple complaints at once.
    Returns individual results + pattern insight.
    """
    results = []
    for transcript in body.transcripts:
        result = await run_pipeline(transcript=transcript, target_lang="en")
        log_interaction(result)
        results.append(result)

    # Aggregate stats
    total = len(results)

    intents    = {}
    decisions  = {}
    sentiments = {}
    languages  = {}
    urgencies  = {}

    for r in results:
        intents[r["intent"]]                          = intents.get(r["intent"], 0) + 1
        decisions[r["decision"]["action"]]            = decisions.get(r["decision"]["action"], 0) + 1
        sentiments[r["sentiment"]]                    = sentiments.get(r["sentiment"], 0) + 1
        languages[r["language_detected"]]             = languages.get(r["language_detected"], 0) + 1
        urgencies[r["urgency"]]                       = urgencies.get(r["urgency"], 0) + 1

    dominant_issue    = max(intents,    key=intents.get)
    dominant_decision = max(decisions,  key=decisions.get)
    dominant_sentiment= max(sentiments, key=sentiments.get)

    escalation_count  = decisions.get("escalate", 0)
    auto_resolve_count= decisions.get("auto_resolve", 0)
    escalation_rate   = round((escalation_count / total) * 100, 1)

    # AI-generated pattern insight
    summary_prompt = f"""
You are an MTN Nigeria operations analyst.
Based on these complaint statistics, write a 2-3 sentence executive insight 
highlighting any patterns, spikes, or recommended actions.

Stats:
- Total complaints: {total}
- Dominant issue: {dominant_issue}
- Escalation rate: {escalation_rate}%
- Sentiment: {sentiments}
- Urgency breakdown: {urgencies}
- Languages: {languages}

Be specific and actionable. Mention if there could be a systemic issue.
"""
    pattern_insight = chat_stream_text([
        {"role": "system", "content": "You are an MTN Nigeria operations analyst."},
        {"role": "user",   "content": summary_prompt},
    ])

    return {
        "total":              total,
        "dominant_issue":     dominant_issue,
        "dominant_decision":  dominant_decision,
        "dominant_sentiment": dominant_sentiment,
        "escalation_count":   escalation_count,
        "escalation_rate":    f"{escalation_rate}%",
        "auto_resolve_count": auto_resolve_count,
        "intent_breakdown":   intents,
        "decision_breakdown": decisions,
        "sentiment_breakdown":sentiments,
        "language_breakdown": languages,
        "urgency_breakdown":  urgencies,
        "pattern_insight":    pattern_insight,
        "results":            results,
    }