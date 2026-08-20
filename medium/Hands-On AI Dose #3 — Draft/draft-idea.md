# Hands-On AI Dose #3 — The Agent Was 30× Too Expensive. Here Are the Receipts. (DRAFT IDEA)

*Instrument the agentic loop with Micrometer, find where the tokens actually go, then cut the bill lever by lever — with measured before/after for every change.*

## Why this article

Dose #2's extraction agent works on both providers, but one meal photo costs ~150–250k tokens
(~5–10¢ on cheap models, dollars on frontier ones). The observed damage from one day of
development: OpenAI 1,665,728 tokens across 31 calls, Anthropic 392,868 in / 12,756 out.
Root cause is architectural, not model-related: the agentic loop replays image + prompt +
all prior tool results on every round, and our tool results are huge.

## Narrative arc: measure → understand → fix → prove

### Part 1 — You can't fix what you can't see (the observability half)
- Spring AI already emits **Micrometer observations** per chat/tool call (model, provider,
  token usage in/out); Spring Modulith observability is already on the classpath.
- Wire them into **actuator metrics** (`/actuator/metrics/gen_ai.client.token.usage`) and
  tag by provider + model + endpoint, so "tokens per photo" becomes a queryable number.
- Screens: metric output before optimization; per-round token growth visualized
  (the quadratic curve of a 10-ingredient plate).
- Optional: a tiny cost gauge — tokens × price table → EUR per request.

### Part 2 — The levers (each applied as its own measured commit)
1. **Slim the tool result**: top-3 candidates, only the ~12 nutrients our schema needs
   (record projection in FdcClient). Expect ~10× smaller tool results, compounding per round.
2. **Batch tool**: `searchFoods(List<String> queries)` — the whole plate in one tool round;
   kills the quadratic replay.
3. **Math out of the model**: model returns `{name, fdcId, quantity}` only; Java computes
   totals from FDC data we already have. Slashes output tokens + kills arithmetic drift.
4. **Prompt caching**: OpenAI auto-caches ≥1k-token prefixes (50% off — verify in metrics);
   Anthropic needs explicit opt-in (Spring AI cacheStrategy — logs showed NONE) for 90% off
   cached input.
5. **Smaller image**: downscale to ~768px before sending; it's re-billed every round.

### Part 3 — The receipts
- Same two fixture photos (apple, bibimbap), same IT, before/after table per lever.
- Target headline: bibimbap from ~200k tokens → **<15k tokens, under a cent**.
- Honest notes: what each lever cost in capability (nothing, ideally) and code (~lines).

## Materials to collect while implementing
- Metric screenshots (actuator + any Grafana/console view)
- Before/after token table per lever (the money shot)
- Featured image: a falling cost curve with the final number

## Rough scope check
Fits one dose if Part 2 is limited to levers 1–4 (5 is one line, mention only).
No new domain features — pure observability + efficiency on the Dose #2 agent.
