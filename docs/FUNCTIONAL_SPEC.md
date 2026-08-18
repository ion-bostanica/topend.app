# Functional Specification — Personal Life Manager

**Owner:** Ion Bostanica
**Status:** Draft v0.1
**Date:** 2026-08-05
**Supersedes:** the current file-and-skill-based `ion-bostanica-fitness-goals` project (which becomes the reference implementation / seed dataset)

---

## 1. Product Definition

### 1.1 One-line

A private, AI-assisted **personal life manager** that unifies nutrition, sleep, training, medical records, habits and long-term goals into a single queryable data layer, and puts a set of specialised agentic coaches on top of it.

### 1.2 Positioning

**Quantified self / personal data OS.** The product's centre of gravity is the *data layer*, not the chat window. Every feature exists to (a) get high-fidelity personal data in, (b) keep it structured, private and durable, and (c) let agents reason over the whole of it rather than one silo at a time.

### 1.3 Design principles

| # | Principle | Implication |
|---|-----------|-------------|
| P1 | **Data ownership first** | All primary data lives in a user-controlled store. Cloud is optional and encrypted. No feature may require surrendering raw data to a third party. |
| P2 | **AI proposes, human disposes** | Every AI-derived value (photo calorie estimate, plan, supplement suggestion) is a *proposal* with a confidence score, persisted separately from confirmed truth. |
| P3 | **Provenance on every datum** | Each stored value records its source (device, API, AI estimate, manual), timestamp, and confidence. Non-negotiable — it is what makes long-horizon pattern analysis trustworthy. |
| P4 | **Recovery and consistency outrank optimisation** | Inherited from `GOALS.md`. Any recommendation engine must respect the user's constitution file before it respects its own optimisation target. |
| P5 | **Local-first, offline-tolerant** | The app is useful on a plane. Sync is a background reconciliation, not a precondition. |
| P6 | **Append-only history** | Corrections create new versions; nothing is silently overwritten. Long-term pattern recognition depends on knowing what was believed when. |
| P7 | **Explainability** | Every recommendation cites the data that produced it. "Do X" is never acceptable without "because Y, Z". |

### 1.4 Non-goals (v1)

- Not a social network. No feed, no friends, no leaderboards.
- Not a clinical decision-support system. No diagnosis, no dosing, no treatment claims.
- Not a multi-tenant SaaS at v1. Single-user, self-hosted or on-device.
- Not a replacement for a physician, therapist, or licensed practitioner.

---

## 2. Domain Model (high level)

The system is organised around **one timeline per person** and a set of typed **records** attached to it.

```
Person
 ├── Streams (continuous, device-sourced)
 │     ├── SleepSession        (stages, HRV, SpO2, respiration, temp deviation)
 │     ├── ActivitySession     (sport, duration, HR zones, power, pace, splits, exercise sets)
 │     ├── DailySummary        (steps, floors, intensity minutes, calories, body battery, stress)
 │     ├── ReadinessSnapshot   (training readiness, training status, load balance, VO2max, race predictions)
 │     └── BodyComposition     (weight, body fat, hydration, BP, RHR)
 ├── Logs (discrete, user- or AI-sourced)
 │     ├── MealEntry           (items, portions, kcal, macros, micros, photo, confidence, confirmed_by)
 │     ├── IntakeEntry         (supplement / medication: substance, dose, unit, time, adherence)
 │     ├── HydrationEntry
 │     ├── SubjectiveEntry     (soreness, mood-free "how do I feel" tags, RPE, pain flags)
 │     └── NoteEntry           (free text / voice, journal)
 ├── Clinical
 │     └── FHIR Resources      (Observation, Condition, MedicationRequest, AllergyIntolerance,
 │                              Immunization, Procedure, DiagnosticReport, DocumentReference)
 ├── Intent
 │     ├── Goal                (metric, target, deadline, priority rank)
 │     ├── Habit               (definition, cadence, streak policy, metric binding)
 │     ├── AbstinenceTrack     (substance/behaviour, start, elapsed, trigger log, urge log)
 │     ├── Roadmap             (ordered milestones under a Goal, with projected vs actual)
 │     └── Constitution        (the rules file: priority order, vetoes, constraints — see §7.1)
 └── Derived
       ├── KnowledgeGraph nodes/edges
       ├── PatternFinding      (detected correlation/regression, evidence, confidence, status)
       └── Plan                (generated training/nutrition/day plan, with rationale + citations)
```

**Key rule:** `Streams` and `Clinical` are read-mostly and machine-sourced. `Logs` and `Intent` are the human's. `Derived` is disposable — it must be fully reconstructible from the other four.

---

## 3. Functional Requirements

### 3.1 Nutrition

**FR-NUT-1 — Photo capture and AI detection.**
User photographs a meal. The system detects dishes and components, estimates portion size, and returns a structured proposal: item list, per-item portion (g/ml), calories, macros (protein/carb/fat/fibre), and micronutrients.

**FR-NUT-2 — Human confirm/edit loop (mandatory).**
The proposal is never committed as truth. The user confirms, edits portions, adds/removes items, or rejects. The system stores *both* the original AI proposal and the confirmed value, so estimator drift can be measured over time.
> *Rationale: the strongest 2026 evidence (NIH metabolic-kitchen study, 102 meals) shows leading photo apps underestimate by 250–345 kcal/meal, with fat the worst offender. A confirm step is not UX polish — it is the accuracy mechanism. See `COMPETITIVE_ASSESSMENT.md` §Nutrition.*

**FR-NUT-3 — Micronutrient tracking.**
Track vitamins, minerals, amino acids and fatty acids, not just macros. Requires a micronutrient-complete food database (USDA FoodData Central as the whole-food base, Open Food Facts for packaged/EU products, with a user-local custom-food table for Moldovan/Romanian items not covered).

**FR-NUT-4 — Text and voice logging.** "Two eggs, 100g oats, coffee" must work as fast as a photo. Recurring meals become one-tap repeats.

**FR-NUT-5 — Targets engine.** Daily calorie/macro targets derived from body mass, training load, day type, and goal phase — the logic currently encoded in `NUTRITION.md`, generalised. Must support fasting windows (e.g. a standing weekly fast) without misreporting an empty log as a deficit warning.

**FR-NUT-6 — Supplements and medications.**
A first-class `IntakeEntry` register: substance, dose, unit, schedule, start/end date, purpose, adherence tracking, and a reminder hook. Must support tapering courses (dose changing over time) and finite courses with an end date. Interactions and contraindications are surfaced as *informational flags with citations*, never as instructions.

**FR-NUT-7 — Nutrition ↔ training coupling.** The nutrition module exposes today's fuelling state to the training module and vice versa (see FR-AGENT-3).

### 3.2 Sleep

**FR-SLP-1 — Garmin first.** Ingest sleep sessions, stages, HRV (overnight + trend), SpO2, respiration, body battery, and sleep score from the Garmin API.
**FR-SLP-2 — Extensible source model.** Sleep is stored in a device-agnostic schema so Oura, Whoop, Apple Health, Withings or a future device can be added as additional providers without schema migration.
**FR-SLP-3 — Subjective overlay.** Nightly self-report (perceived quality, wake count, aids used) stored alongside device data; divergence between the two is itself a tracked signal.
**FR-SLP-4 — Sleep as a first-class input.** Sleep debt, HRV trend, and consistency feed directly into training recommendations and pattern detection.

### 3.3 Training metrics & performance

**FR-TRN-1 — Garmin first.** Activities, splits, exercise sets, HR/power zones, training readiness, training status, load balance, VO2 max trend, lactate threshold, race predictions, endurance/hill scores, personal records.
**FR-TRN-2 — Never re-fetch archived dates.** Ingestion is idempotent and incremental; already-archived days are read from local store. (Inherited operational rule from the current project.)
**FR-TRN-3 — Non-device sports.** Taekwondo, mobility, and home drills must be loggable with sport-specific metrics (technique reps, speed drills, session RPE, partner/solo flag) even when the watch records them as generic "strength".
**FR-TRN-4 — Performance modelling.** Track load (acute:chronic), fitness/fatigue/form, and per-goal progression curves.
**FR-TRN-5 — Write-back.** Structured workouts generated by the planner can be pushed to the watch as scheduled workouts.

### 3.4 Goal progress tracking

**FR-GOL-1 — Metric-bound goals.** Every goal binds to a measurable metric with a target value and deadline (e.g. *VO2 max 46 → 55 by 2026-12-31*; *half marathon < 2:00:00 by 2026-12-31*).
**FR-GOL-2 — Priority ordering.** Goals carry an explicit priority rank; the planner resolves conflicts by rank, not by recency.
**FR-GOL-3 — Roadmaps.** A goal decomposes into ordered milestones with expected-by dates. The system computes required-rate vs actual-rate and projects a landing date.
**FR-GOL-4 — On-track status.** Each goal shows one of: *ahead / on track / at risk / off track / stalled*, with the evidence behind the call.
**FR-GOL-5 — Per-area dashboards.** One main dashboard, plus a drill-down dashboard per life area (Training, Nutrition, Sleep, Health, Habits, Discipline, Time). See §5.

### 3.5 EHR — local, encrypted, FHIR

**FR-EHR-1 — On-device encrypted FHIR store.** Clinical records stored as FHIR R4 resources in an encrypted local database. Encryption key derived from a user secret + device keystore; the sync layer must never hold plaintext.
**FR-EHR-2 — Import from compatible apps.** Support (in priority order):
 1. Manual import of FHIR bundles / C-CDA / PDF lab reports (works everywhere, day one).
 2. Apple Health Records → HealthKit clinical types (US/UK/CA coverage).
 3. Android Health Connect Medical Records (FHIR read APIs).
 4. Direct SMART-on-FHIR connections to individual provider endpoints.
 5. National portals as they mature — Romania's *dosarul electronic de sănătate* (opening to patients 2026), Moldova's DES (still in planning as of mid-2026), and EHDS/MyHealth@EU cross-border access (primary-use obligations from March 2029).
**FR-EHR-3 — Lab results as time series.** DiagnosticReport/Observation values must join the same timeline as wearable data so a ferritin trend and an HRV trend can be plotted together.
**FR-EHR-4 — Export and portability.** One-click export of the complete record as a FHIR bundle. No lock-in.
**FR-EHR-5 — Explicit consent boundary.** Clinical data is never sent to a third-party LLM provider without a per-session, per-scope, explicitly granted consent, and the app must show exactly what was shared. Default is local-model-only for this domain.

### 3.6 Habits

**FR-HAB-1 — Metric-based definition.** A habit is defined by a measurable condition, not a checkbox (e.g. *mobility ≥ 4 sessions/week*, *protein ≥ 145 g on ≥ 6 of 7 days*).
**FR-HAB-2 — Auto-verification.** Where the condition can be verified from ingested data, it is — no manual ticking. Manual confirmation is the fallback, not the default.
**FR-HAB-3 — Streak policy per habit.** Configurable: strict streak, rolling N-of-M window, or trailing average. Missing one day must not be able to destroy a quarter of accumulated evidence.
**FR-HAB-4 — Habit ↔ outcome linkage.** Each habit optionally declares which Goal it serves, so the dashboard can show *habit adherence → goal movement*.

### 3.7 Addiction / dependency management

**FR-ADD-1 — Abstinence and reduction tracks.** Support both models: full abstinence (elapsed time, milestones) and managed reduction (target quantity/frequency per period, trend vs target).
**FR-ADD-2 — Metric-based, private by default.** Tracked as ordinary metrics on the timeline. This data is flagged **highest sensitivity**: excluded from any cloud sync unless explicitly opted in, excluded from third-party model calls by default, and hidden from shared/exported dashboards unless deliberately included.
**FR-ADD-3 — Trigger and urge logging.** Timestamped context (time of day, location class, preceding activity, sleep debt, stress) captured around urges, so pattern detection can find antecedents.
**FR-ADD-4 — Correlation surface.** Because this data sits on the same timeline as sleep/training/nutrition, the system can surface relationships no single-purpose recovery app can (e.g. urge frequency vs sleep debt vs training load).
**FR-ADD-5 — Safety boundary.** The system does not provide clinical treatment, does not attempt crisis intervention, and surfaces professional-support signposting where relevant. It is a measurement tool, not a therapy.

### 3.8 Long-term goals and roadmaps

**FR-LTG-1 — Multi-year horizon.** Goals may span years; the data model must not assume an annual reset.
**FR-LTG-2 — Blocks and phases.** Support periodised blocks (the current project's 12-week block structure generalised) with per-block targets rolling up to the long-term goal.
**FR-LTG-3 — Retrospectives.** At block end, an automatic retrospective: what was planned, what happened, what moved, what didn't, and what the next block should change.
**FR-LTG-4 — Visual on-track dashboards.** See §5.

---

## 4. Agentic Layer

### 4.1 RAG and knowledge graph over all data

**FR-AI-1 — Unified retrieval.** A single retrieval layer spanning structured metrics, free-text notes, meal logs, clinical documents, and plan history. Natural-language queries must be answerable across domains ("how did my HRV behave in the two weeks after I started the tapering course?").
**FR-AI-2 — Knowledge graph.** Entities (foods, supplements, exercises, symptoms, conditions, goals, habits, places, sessions) and typed relations between them. The graph is what turns "a pile of logs" into something an agent can reason over — and it is the main technical differentiator against every product surveyed in the competitive assessment.
**FR-AI-3 — Temporal awareness.** Retrieval must be time-aware: "recently", "since the block started", "compared to last year" resolve correctly.
**FR-AI-4 — Citation requirement.** Every agent answer cites the underlying records. No uncited health claims about the user's own data.

### 4.2 Agentic personas (coaches)

**FR-AI-5 — Multiple specialised coaches** sharing one memory and one data layer:

| Coach | Owns | Reads |
|-------|------|-------|
| **Fitness** | Training plan, load management, sport-specific progression | Training, sleep, readiness, nutrition, pain flags |
| **Nutrition** | Targets, meal quality, micronutrient gaps, fuelling timing | Nutrition, training, labs, body composition |
| **Wellness / Recovery** | Sleep, stress, recovery protocols, deload calls | Sleep, HRV, stress, load, subjective |
| **Health** | Labs, biomarker trends, screening cadence, medication adherence | EHR, intake register, body composition |
| **Discipline** | Habits, streaks, dependency tracks, follow-through | Habits, abstinence tracks, plan adherence |
| **Time Management** | Scheduling, calendar fit, realistic day planning | Calendar, plan, session durations, commute/travel |

**FR-AI-6 — Arbitration.** Coaches will disagree (Fitness wants a hard session; Wellness sees a poor recovery trend). Conflicts resolve through the **Constitution** (§7.1) priority order, and the resolution is shown to the user with both positions stated.
**FR-AI-7 — Shared memory.** Coaches share long-term memory about the user; they do not each maintain a private, divergent model.
**FR-AI-8 — Consistent voice, distinct competence.** Personas differ in *what they know and optimise for*, not in gimmicky personality. No sycophancy; a coach that only agrees is useless.

### 4.3 On-demand agentic planning

**FR-AI-9 — Plan on request.** "Plan my week", "what's my training today", "what should I eat tonight", "build me a 12-week block" — each produces a concrete, dated, executable plan grounded in all data to date.
**FR-AI-10 — Constitution compliance.** Every plan is validated against the rules file before it is shown (e.g. no two hard days in a row; pain vetoes explosive work; respect the fasting window). A plan that violates a rule is rejected and regenerated, not shown with a caveat.
**FR-AI-11 — Rationale required.** Each planned session explains which goal it serves and why it was chosen over the alternative.
**FR-AI-12 — Replanning.** Plans are living: a missed or downgraded session triggers a proposed adjustment to the rest of the week rather than silent drift.

### 4.4 Supplement recommendations

**FR-AI-13 — Context-aware suggestions** based on: training load and HR-zone distribution, sweat/electrolyte loss estimated from session type + weather (temperature, humidity, UV, air quality), measured nutritional gaps from the micronutrient ledger, lab biomarker deficiencies from the EHR, sleep quality, and the current intake register.
**FR-AI-14 — Evidence-graded output.** Each suggestion carries an evidence grade, the specific gap it addresses, expected effect, and known interactions with what's already in the register.
**FR-AI-15 — Hard safety rail.** Suggestions are informational, always cite sources, never prescribe dosing for a medical condition, and always recommend professional consultation where a clinical question is implicated.

### 4.5 Long-term pattern recognition

**FR-AI-16 — Background analysis.** Periodic scans across the full timeline for: performance regressions, recurring injury antecedents, sleep degradation trends, nutritional deficits, habit decay, dependency creep, and seasonal effects.
**FR-AI-17 — Cross-domain correlation.** The point of the unified store: *late eating → sleep fragmentation → next-day readiness → session quality*, or *low ferritin period → VO2 plateau*. Findings are presented as hypotheses with strength and evidence, never as established causation.
**FR-AI-18 — Actionable remediation.** Each confirmed pattern proposes a specific, testable intervention with a review date, which can be promoted to a Habit or a Goal.
**FR-AI-19 — Statistical honesty.** Correction for multiple comparisons; minimum-evidence thresholds before a pattern is surfaced; explicit "not enough data yet" state.

---

## 5. Dashboards

**FR-DSH-1 — Main dashboard.** One screen answering: *am I on track, overall, right now?* — with a per-area status tile (Training, Nutrition, Sleep, Health, Habits, Discipline) and today's single most important action.
**FR-DSH-2 — Per-area dashboards.** Each area drills into its own goals, trends, and habits.
**FR-DSH-3 — Goal roadmap view.** Milestone timeline with required-rate vs actual-rate and projected landing date.
**FR-DSH-4 — Timeline / correlation view.** Any two or more metrics overlaid on a common time axis, with event annotations (block starts, illness, travel, courses started/ended).
**FR-DSH-5 — Data-honest visuals.** Confidence bands on estimated values; visual distinction between measured, confirmed, and AI-estimated data.

---

## 6. Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-1 | **Privacy.** Local-first storage. Encryption at rest for all personal data; separate, stricter key handling for clinical and dependency data. |
| NFR-2 | **Model routing.** Per-domain policy for which model may see which data. Clinical and dependency data default to local models; general planning may use hosted models on explicit consent. |
| NFR-3 | **Offline.** Logging, viewing, and cached plans work with no network. Ingestion and agent calls queue and reconcile. |
| NFR-4 | **Idempotent ingestion.** Re-running a sync must not duplicate or mutate archived data. |
| NFR-5 | **Auditability.** Every write records actor (user / agent / ingestor), timestamp, and source. |
| NFR-6 | **Secrets.** Credentials (Garmin, provider tokens) live in an OS keychain / secret store — never in a repo file, never printed. |
| NFR-7 | **Latency.** Meal photo → proposal in under 5 s. Dashboard load under 1 s from local store. |
| NFR-8 | **Portability.** Full data export in open formats (FHIR bundle for clinical, JSON/CSV for the rest) at any time. |
| NFR-9 | **Graceful degradation.** If a provider API is down or unauthorised, the app falls back to the last archived snapshot and says so explicitly. |
| NFR-10 | **Multi-surface.** Mobile is the capture surface (photos, quick logs, voice); desktop/web is the analysis surface. |
| NFR-11 | **Localisation.** Europe/Chisinau timezone handling, metric units, and food database coverage for Moldovan/Romanian products. |

---

## 7. Carry-over from the current project

The existing `ion-bostanica-fitness-goals` folder is the working prototype. These concepts must survive the migration:

### 7.1 The Constitution
`GOALS.md` functions as a constitution: a priority-ordered set of goals plus hard rules that override any optimiser. This becomes a first-class, versioned entity (`Constitution`) that the planner validates against. Current content to migrate: priority order (recovery/consistency → half marathon → VO2 max → taekwondo speed → mobility/longevity); rules (no two hard days in a row; poor recovery ⇒ reduce intensity; pain ⇒ no explosive work; mobility weekly, not only on rest days).

### 7.2 Structured personal context
Facts the current system encodes in prose that must become structured data: weekly training rhythm with seasonal variants; facility availability windows (dojo open days, which change by season); standing fasting windows; block boundaries and block history; supplement/medication courses with start and end dates.

### 7.3 Operational rules
- Never re-fetch already-archived data.
- Credentials stay in one place and are never copied or printed.
- Historical blocks are archived, not deleted — they are training data for pattern recognition.

### 7.4 Migration
Existing `meals/`, `garmin/`, `JOURNAL.md`, `dashboards/`, and `archive/` become the seed dataset. Migration is a first-class task, not an afterthought: the value of the pattern-recognition layer scales with history length, so back-loading existing data matters from day one.

---

## 8. Open Questions

1. **Platform.** Native mobile (iOS-first, for HealthKit + camera + LiDAR portion estimation) vs. cross-platform vs. local server + PWA?
2. **Photo estimation stack.** Licensed vision API (Passio-class) vs. general multimodal model with a strong food database behind it vs. hybrid? Depth/LiDAR portion sensing materially reduces the dominant error source and is worth testing early.
3. **Local model feasibility.** Which parts of the agentic layer can run on-device at acceptable latency, and what is the fallback policy when they can't?
4. **Knowledge graph substrate.** Property graph vs. RDF vs. relational-with-graph-views; and how the graph stays in sync with an append-only timeline.
5. **Scope of v1.** All eight domains at shallow depth, or two domains (training + nutrition, already proven in the prototype) at full depth with the rest scaffolded?
6. **Regulatory posture.** Staying firmly in "wellness tool" territory vs. anything that could be read as a medical device — the boundary is where this becomes expensive.
7. **Distribution.** Personal-only, open-source-and-self-hosted, or productised — this decision changes the architecture (single-tenant vs. multi-tenant) and should be made before the data layer is built.
