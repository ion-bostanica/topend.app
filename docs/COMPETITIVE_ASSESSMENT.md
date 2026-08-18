# Competitive Assessment — Personal Life Manager

**Prepared for:** Ion Bostanica
**Date:** 2026-08-05
**Scope:** the market a whole-life AI health/wellness manager would enter, across five adjacent categories, plus the gaps worth building into.

---

## 0. Executive summary

**The market is wide but shallow.** Every capability on the requirements list exists somewhere in 2026 — usually done well — but the products are siloed by *data source* rather than by *user need*. Wearable companies build coaches over their own sensor data. Nutrition apps build over food databases. Habit trackers build over checkboxes. Personal-health-record companies build over FHIR. Almost nobody joins them.

**Five findings that should shape the build:**

1. **The unified data layer is the actual moat, not the AI.** Every AI coach shipping in 2026 — Whoop Coach, Oura Advisor, Garmin Active Intelligence, Google Health Coach, Ultrahuman Jade — is scoped to its vendor's own first-party data. That is a structural limitation, not a temporary one: they can't read your labs, your habits, or your Garmin data because it isn't theirs.
2. **Habits and dependency tracking are essentially absent from the health-data world.** Habit trackers don't read biometrics (only Streaks does, via HealthKit, and reportedly unreliably). Recovery/abstinence apps are streak-counters with no wearable connection. Nothing correlates urge frequency against sleep debt or training load. This is the clearest white space found.
3. **AI photo calorie estimation is worse than marketed, and this is a design constraint, not a vendor-selection problem.** The 2026 NIH metabolic-kitchen study found leading apps (MyFitnessPal, Lose It!, Cal AI, Appediet) underestimate by **250–345 kcal per meal** and fat by ~30 g. Human-in-the-loop correction still beats unassisted AI. The confirm/edit step in the spec is the accuracy mechanism.
4. **Micronutrients are a genuine differentiator.** Almost no photo-first app tracks them. Cronometer owns this (80+ nutrients) and MacroFactor recently added it — neither has an AI-first capture flow. Photo capture + micronutrient depth is an unoccupied combination.
5. **"Life OS" is unclaimed branding.** The term is currently owned by static Notion templates ($15–39, manual entry). No funded consumer product has claimed a data-native life OS. Meanwhile the tailwind is real: health coaching is a ~$24B market growing >10% CAGR, and AI took 55% of health-tech VC funding in 2025 (up from 37% in 2024).

**The honest risk:** Google Health (relaunched May 2026, Gemini coach at $9.99/mo, includes medical records) is the one player structurally capable of building this. It's Fitbit/Pixel-first today, which buys time, but it is the competitor to watch.

---

## 1. Category A — Wearable-native AI coaches

These are the closest thing to the product's ambition, and the most instructive about its limits.

| Product | AI layer | Pricing (2026) | Garmin? | Domains covered | Key weakness |
|---|---|---|---|---|---|
| **Whoop** | Whoop Coach (GPT-4-based, persistent memory since 2025) | One $199/yr · Peak $239/yr · Life $359/yr | No | Recovery, strain, sleep; nutrition only via 3rd-party sync | Closed loop — cannot reason beyond its own sensor; no Garmin, no labs, no habits |
| **Oura** | Oura Advisor (public Apr 2025; conversational memory, action plans) | Ring + subscription | No | Sleep, readiness, activity, cycle | No native nutrition or training plan; closed ecosystem |
| **Ultrahuman** | Jade AI — "real-time biointelligence," *takes actions* (triggers breathwork, AFib flags); free to all users | Ring Air ~$210–349 · Ring Pro $479, no subscription · M1 CGM $99/mo · Blood Vision $99/6mo–$499/yr | Not native | Sleep, glucose, blood biomarkers, cardio, training via PowerPlugs | Hardware-locked; PowerPlugs marketplace still thin |
| **Garmin Connect+** | "Active Intelligence" insights | $6.99/mo · $69.99/yr | Native (it *is* Garmin) | Training, sleep, readiness | Thin AI; heavy user backlash over paywalling |
| **Strava** | Athlete Intelligence (30-day trends, PB detection) | Subscriber-only | Reads Garmin activities | Training analysis only | Single-domain; no recovery/nutrition/health |

**Read-across:** the AI in this category is a conversational wrapper over one vendor's telemetry. None of them can answer "did my ferritin dip cause the VO2 plateau?" because they never see a lab result. That question is the product thesis.

---

## 2. Category B — Multi-source health aggregators (the direct competitors)

| Product | Coverage | Pricing | Garmin? | Notes |
|---|---|---|---|---|
| **Gyroscope (One)** | Sleep, activity, HR, workouts, blood metrics (Dexcom/Stelo/doctor PDFs), 23andMe DNA, "Food XRAY" photo nutrition, mood, single Health Score | Not publicly listed | **Yes** | Closest existing product to the concept. Broadest ingestion found. Long-running, niche. |
| **Bevel** | Recovery/sleep/strain/stress, nutrition (barcode/photo/recipe), 700+ exercises, cycle, biological age, Health Records lab import | Free · Pro $14.99/mo or $99.99/yr | HealthKit-mediated | "Bevel Intelligence" AI coach is **rate-limited by weekly allowance** — a telling constraint on LLM cost economics |
| **Vora** | 500+ platforms incl. Garmin, Oura, Whoop, Fitbit, Strava, Samsung, MyFitnessPal; voice-first coaching with 6 coach personalities | Free · Pro $12.99/mo or $89.99/yr | **Yes** | Closest thing found to multi-persona coaching in a health app |
| **4sight** | 4 pillars (Move/Fuel/Rest/Rise), 150+ metrics; **"Galen AI" explicitly RAG-powered** | Free · Premium $6.67/mo ($79.99/yr) | **"Coming soon"** — not live | The only vendor openly marketing RAG over personal health data |
| **Welltory** | HRV-centric; generates "personalised research papers" from your data | $99/yr · $399 lifetime | No | Analysis-report model rather than conversational coach |
| **Athlytic** | Recovery only; iOS/Apple Watch **only** | Free · Pro ~$24.99/yr | Explicitly **no** | Deliberately single-domain |

**Read-across:** Gyroscope, Bevel and Vora already do multi-source aggregation with an AI layer. What none of them do: **structured goals with roadmaps, habit auto-verification against biometrics, dependency tracking, or a real knowledge graph.** 4sight claims RAG but doesn't have Garmin live yet.

---

## 3. Category C — Nutrition and photo logging

### The accuracy picture (this is the important part)

| Source | Finding |
|---|---|
| **NIH / NIDDK metabolic kitchen (NUTRITION 2026)** | 102 meals weighed to 0.1 g vs. MyFitnessPal, Lose It!, Cal AI, Appediet: apps **underestimated by 250–345 kcal/meal** and fat by ~30 g. Confirmed on a 200+ meal follow-up. Worst errors on high-fat dishes — cooking oil is invisible to a camera. |
| **2026 literature review** | Food *identification* 85–95% top-1 on common single items. Portion estimation from a single 2D photo carries **15–25% error** — the dominant error source — falling to 5–10% **with depth/LiDAR sensing**. |
| **Third-party benchmark (500 standardised images)** | MyFitnessPal Meal Scan 71.2% ID accuracy, ±18% portion error; drops to 58.3% on East Asian and 61.1% on South Asian cuisine. Lose It! Snap It 68.7%, ±22%. |
| **SnapCalorie (CVPR 2021, Google Research co-authored)** | ~15% mean caloric error on a curated 5,000-dish dataset — vs. ~20% for nutrition labels, ~40% for dietitians, ~53% for typical user self-report. Best published number, but curated conditions. |
| **ZOE photologging (10,000 meals, 2,100+ participants)** | Strong on tea/coffee (0.88) and vegetables (0.87), weak on plant milk (0.13) and soup (0.07). Macro bias: +7% calories. |

**Three design conclusions:**
1. **Never auto-commit an AI estimate.** Confirm/edit is the accuracy mechanism, and storing proposal-vs-confirmed lets you measure and correct estimator bias over time — something no competitor does.
2. **Depth/LiDAR portion sensing is the single highest-leverage technical bet** available, cutting the dominant error source roughly in half. iOS-first has a real justification here.
3. **Regional cuisine is a known failure mode.** Moldovan/Romanian dishes will be in the weak tail of every off-the-shelf model. A local custom-food table isn't a nice-to-have.

### The players

| App | Pricing | Micronutrients | Supplements/meds | Weakness |
|---|---|---|---|---|
| **Cronometer** | Free · Gold ~$50/yr | **Yes — 80+ nutrients**, deepest in category | **Yes**, dedicated supplement diary | Slow manual logging, no fast photo path |
| **MacroFactor** | $11.99/mo · $71.99/yr, no free tier | **Yes** — "Nutrient Explorer", 26,500-entry micronutrient-complete DB | No | No AI photo scan at all |
| **MyFitnessPal** | Premium $19.99/mo · $79.99/yr | Minimal | No | Crowdsourced DB quality; weak photo accuracy for the price |
| **Lose It!** | $39.99/yr | Basic macros | No | Weakest photo accuracy of the majors |
| **Cal AI** | ~$10–20/mo | Minimal | No | Marketing-led black box; 30–50% error on mixed dishes; refund complaints |
| **SnapCalorie** | Free (3 logs/day) + premium | Not emphasised | No | Best published accuracy, smallest food DB |
| **Yuka** | ~€10/yr | No (composite score only) | No | Barcode scoring, not a tracker at all |
| **Passio.ai** | Enterprise licensing | DB-dependent | N/A | B2B vision API — the "arms dealer" behind many consumer apps; a viable build-vs-buy option |

**Data sources available free:** USDA FoodData Central (gold standard for whole foods, free API), Open Food Facts (open ODbL, best packaged/EU coverage, quality varies), Nutritionix (opaque pricing, daily rate caps), Edamam (recipe-centric).

**The gap:** *nobody combines fast photo capture with real micronutrient depth and a supplement/medication register.* Cronometer has the depth and no speed; Cal AI has the speed and no depth; neither tracks supplements properly at all.

---

## 4. Category D — Personal health records (EHR / FHIR)

### What's actually achievable for a solo developer in 2026

**Realistic, low/zero cost:**
1. **Apple Health Records** — FHIR R4 (+ legacy DSTU2), US Core 3.1.1; AllergyIntolerance, Condition, clinical notes, Immunization, MedicationRequest, Observation, Procedure across 15+ EHR vendor types (US/CA/UK). Reachable by a third-party app only through HealthKit with user consent — which is exactly the local-first model the spec wants.
2. **Android Health Connect Medical Records** — FHIR-format read/write APIs with a permissions UI, shipped as part of the May 2026 Google Health relaunch.
3. **Direct SMART-on-FHIR** against individual provider endpoints, using open clients (`client-js` from SMART Health IT, or `fhir-kit-client` for Node). Zero recurring cost, real engineering effort. Many providers now expose these because of CMS/Info-Blocking rules.
4. **Metriport** — the one open-source, dev-friendly aggregator: Apache-licensed, public GitHub, CommonWell/Carequality HIE data in FHIR R4/C-CDA/PDF, self-serve dashboard and sandbox.

**Not viable for a personal project:** Flexpa's cheapest tier is **$20,000/year**; 1upHealth is quote-only with no self-serve tier; Particle Health, Health Gorilla, Zus, b.well, Seqster, Commure are all enterprise sales-gated. Human API is now inside LexisNexis Risk Solutions and sells to insurers.

### Regulatory state

- **US:** CMS-0057-F mandates FHIR Patient Access APIs from payers. Information-blocking enforcement is described by health-law firms as having genuinely started in 2026. TEFCA **Individual Access Services** is the patient-facing pathway — but Epic's consumer-facing MyChart Central authorisation flow only launches **late 2026**, and the AHA has asked for a delay on the IAS procedure over privacy concerns. Real self-service patient pulls are *just now* becoming usable.
- **EU:** **EHDS Regulation (EU) 2025/327** is in force, but staged: implementing acts March 2027, primary-use obligations (patient summaries, e-prescriptions via MyHealth@EU) **March 2029**, imaging/labs/discharge **March 2031**. Citizens get a codified right to immediate, free, machine-readable access to their own data — in 2029.
- **Romania:** the national *dosarul electronic de sănătate* is reported to open to all patients from **summer 2026**, web + mobile, replacing a 20-year-old CNAS backend.
- **Moldova:** still at the **consultation/planning stage** for a national DES as of April 2026 — no published timeline or budget. A separate CNAS "e-Sănătatea Mea" patient portal for prescriptions and referrals is rolling out near-term. **Net: Moldova is meaningfully behind Romania.**

**Implication for the spec:** FR-EHR-2's priority order (manual bundle/PDF import → HealthKit → Health Connect → SMART on FHIR → national portals) is the right sequencing. Manual import must work on day one, because for a Moldova-based user the automated pathways largely don't exist yet.

### On-device encrypted FHIR — does anyone ship it?

Yes. **Google's Android FHIR SDK / Open Health Stack** stores FHIR resources in local SQLite with SQLCipher application-layer encryption on top of Android full-disk encryption, syncing to a developer-chosen FHIR server. That's a genuine local-first encrypted architecture available today. Apple keeps Health Records in the encrypted HealthKit store but publishes no third-party on-device FHIR spec.

**Open-source stack recommendation** (from the research): **Medplum** (Apache-2.0, FHIR-native, TypeScript SDK, React components, SMART-on-FHIR auth, self-hostable — best fit for a solo dev) + **fhir-kit-client / client-js** for provider connections + **Android FHIR SDK**-style encrypted local cache if going mobile. HAPI FHIR is the mature heavyweight (JVM). **Fasten Health** is worth studying as a personal-PHR UI reference, but its self-hosted OSS version **cannot pull from provider portals** — only manual FHIR bundle import, or its paid Connect tier.

---

## 5. Category E — Habits, dependency tracking, and AI coaching

### Habits & goals

| App | Pricing | Reads wearable data? |
|---|---|---|
| Habitica | Free · ~$5/mo | No |
| **Streaks** | $5 one-time | **Yes** — HealthKit steps/meditation, but "sometimes drops data" |
| Way of Life | $5 one-time · $4/mo Pro | No |
| Finch | Free · ~$40–70/yr | No |
| Atoms (James Clear) | Free · $4/mo | No |
| Sunsama | $20–25/mo | N/A (planning tool) |
| Reclaim.ai | Free · $8–12/user/mo | No |
| Structured / Routinery | $2.49–7.99/mo | No |
| Notion "Life OS" templates | $15–39 per template | No — manual entry only |

**One product in the entire category reads biometric data, and it does so unreliably.** Habit tracking is entirely disconnected from the health-data world.

### Dependency / abstinence tracking

| App | Pricing | Mechanics |
|---|---|---|
| I Am Sober | Free · $9.99/mo · $39.99/yr | Day counter, money/calories saved, mood + trigger log, up to 10 tracks |
| Reframe | $13.99/mo | Neuroscience curriculum, quantity/timing logging, claims 25% average reduction |
| Sunnyside | $8.99/mo | Moderation model, weekly coaching check-ins, mood tracking |
| Less | $5.99/mo | Streaks, self-paced, meditation library |
| Nomo | **Free** | Unlimited independent minute-precision clocks for any habit |
| Loosid | Free · ~$20–80 | Social-network-first, tracking secondary |

**None connect to wearable or health data.** The clinical tier tells a cautionary tale: **Pear Therapeutics** (FDA-cleared reSET/reSET-O, ~$400M raised) went bankrupt in 2023 on reimbursement failure; PursueCare bought the clearances and relaunched them in 2024 as a bundled tool inside its own virtual care. Prescription digital therapeutics as a standalone business model failed. The surviving tier is cheap ($0–14/mo) consumer self-tracking.

**The unoccupied position:** dependency tracking on the *same timeline* as sleep, training, nutrition and stress — so urge frequency can be correlated against sleep debt and load. No product does this.

### AI coaching

| Product | Pricing | Reads your data? |
|---|---|---|
| Rocky.ai | From $29/mo individual · $99–199/mo teams | No biometrics |
| Wysa | Free · $74.99/yr · +human coaching $79.99/mo | No biometrics |
| **Youper** | $69.99/yr | **Shutting down 30 Sept 2026** |
| Mindsera | Free · $9.99/mo · $79.99/yr | No — journaling only |
| Ash (Slingshot AI) | Consumer | Psychology-specific foundation model; **$93M raised**, a16z-backed |
| BetterUp AI Coach | Enterprise | Launched Jan 2025, AI + human coach network |
| InnerForge | Cheaper than Rocky/BetterUp | **10 specialised coaches** (Career, Money, Habits, Productivity, Resilience…) — but a thin LLM wrapper, no data platform |
| Whoop Coach / Oura Advisor | Bundled | **Yes** — the state of the art, but only their own sensor data |
| Fitbod | $15.99/mo · $95.99/yr | Training history only |

**Two observations.** First, the coaching products that read real data (Whoop, Oura) are single-vendor; the ones with breadth (Rocky, Wysa, InnerForge) read nothing. Second, **Youper's shutdown** is a live signal that standalone AI-chatbot wellness products are consolidating — conversation alone is not a defensible product.

**Multi-persona coaching is essentially unclaimed.** A comparison surveying BetterUp, CoachHub, Rocky.ai, Wysa, Pocketcoach and Reflectly found *none* combining fitness + wellness + discipline + time management. InnerForge is the nearest analogue and it's a prompt wrapper.

---

## 6. Market context

- Health coaching overall: **$22.04B (2025) → $24.1B (2026) → $35.6B by 2030**, 10.2% CAGR — accelerating, with AI cited as the driver.
- **AI captured 55% of all health-tech VC funding in 2025**, up from 37% in 2024. 527 deals, ~$14B; average deal size +42% YoY.
- Health-tech reached **$121B combined market cap** in 2025 with 6 IPOs raising $36.6B.
- Consumer cash-pay is outpacing insurance-reimbursed as the adoption path (36% opting to pay out of pocket for AI-enhanced screening).
- Informal demand is already mainstream: **~1 in 6 US adults** use AI chatbots monthly for health advice; 48.7% of AI users with mental-health challenges have used an LLM for support.

---

## 7. Where this product wins, and where it's exposed

### Defensible differentiation

| # | Position | Why it holds |
|---|----------|--------------|
| D1 | **Cross-source knowledge graph** | Vendor coaches are structurally locked to first-party data. Only 4sight claims RAG, and it doesn't have Garmin live. |
| D2 | **Habits + dependency tracking joined to biometrics** | Zero products do this. The correlation surface (urges vs. sleep debt vs. load) is genuinely novel. |
| D3 | **Photo capture + micronutrient depth + supplement register** | Cronometer has depth without speed; Cal AI has speed without depth; nobody has a proper supplement/medication register. |
| D4 | **Constitution-governed planning** | Priority-ordered goals with hard vetoes (no two hard days in a row; pain vetoes explosive work) that the planner must satisfy. Every competitor's AI gives advice; none is bound by a rules file it cannot override. |
| D5 | **Multi-persona coaches over one shared memory** | The two halves exist separately in the market (InnerForge has personas without data; Whoop has data without personas). |
| D6 | **Local-first encrypted ownership** | A real position against Google/Whoop/Oura, and the only credible one for clinical + dependency data. |
| D7 | **Measured estimator bias** | Storing AI proposal vs. human confirmation and correcting over time directly attacks the documented 250–345 kcal/meal error. Nobody does this. |

### Real risks

| # | Risk | Mitigation |
|---|------|------------|
| R1 | **Google Health** (May 2026, Gemini coach $9.99/mo, includes medical records) is structurally able to build this | It's Fitbit/Pixel-first and platform-generic. Compete on depth, ownership, and goal/constitution rigour — not on breadth of device support. |
| R2 | **Scope is enormous** — 8 domains, RAG, KG, 6 personas, FHIR | Sequence it. Training + nutrition are already proven in the prototype and have a real dataset behind them. |
| R3 | **LLM cost economics** — Bevel already rate-limits its AI coach by weekly allowance | Local models for routine work; hosted models only for planning and deep analysis. |
| R4 | **Photo accuracy will disappoint** if presented as authoritative | Design honesty: confidence bands, confirm-first, visible provenance. Turn the weakness into the trust story. |
| R5 | **EHR access in Moldova barely exists yet** | Manual import first. Treat national-portal integration as a 2027–2029 roadmap item, not a v1 dependency. |
| R6 | **Standalone AI-wellness products are consolidating** (Youper shutting down Sept 2026; Pear's bankruptcy) | Don't build a chatbot. Build the data layer; the chat is a view over it. |
| R7 | **Regulatory drift** — supplement and biomarker advice edges toward medical-device territory | Keep advice informational and cited; no dosing for conditions; explicit professional-consultation signposting. |

---

## 8. Recommended sequencing

1. **Data layer + Garmin + nutrition first.** These are already proven in the prototype and there's real history to migrate. Nothing downstream works without the timeline.
2. **Goals, roadmaps, constitution.** Cheap to build, immediately differentiating, and it's what makes the coaches non-generic.
3. **Habits with auto-verification.** The white space, and it needs only the data layer that already exists by then.
4. **Knowledge graph + RAG.** Once there's enough history for cross-domain questions to have answers.
5. **Personas.** After the data and the constitution exist — otherwise they're a prompt wrapper, which the market has already shown is not a product.
6. **EHR / FHIR.** Manual import early (it's cheap and unblocks lab-vs-performance correlation); automated pulls when the regional infrastructure catches up.
7. **Dependency tracking.** Technically trivial once habits exist; the value is entirely in the correlation surface, which needs everything above it.

---

## Sources

**Wearables & aggregators:** [Whoop pricing](https://trackervs.com/pricing/whoop-pricing/) · [Whoop Advanced Labs](https://www.bloodtestcomparison.com/whoop-advanced-labs) · [Whoop Coach limits](https://the5krunner.com/2025/10/31/2026-whoop-5-0-mg-review-discount-accuracy-strain-recovery-athletes/) · [Oura Advisor](https://ouraring.com/blog/oura-advisor/) · [Oura Advisor launch](https://www.wareable.com/news/oura-advisor-puts-ai-coaching-on-hand) · [Ultrahuman Ring Pro + Jade AI](https://cyborg.ultrahuman.com/press-releases/ultrahuman-unveils-ring-pro-with-category-defining-15-day-battery-and-jade-worlds-first-real-time-biointelligence-ai) · [Ultrahuman PowerPlugs](https://www.ultrahuman.com/us/powerplugs/) · [Ultrahuman Blood Vision](https://www.ultrahuman.com/us/blood-vision/buy/) · [Garmin Connect+](https://www.techradar.com/health-fitness/garmin-adds-premium-garmin-connect-tier-with-ai-features-but-promises-your-free-experience-is-not-going-away) · [Garmin paywall backlash](https://tomsguide.com/wellness/smartwatches/garmin-sparks-outrage-with-connect-subscription-paywall-have-your-say) · [Strava Athlete Intelligence](https://press.strava.com/articles/stravas-athlete-intelligence-translates-workout-data-into-simple-and) · [Gyroscope One](https://gyrosco.pe/one/) · [Bevel Intelligence limits](https://help.bevel.health/en/articles/11583937) · [Vora](https://askvora.com/) · [4sight / Galen AI](https://4sight.fit/) · [Welltory pricing](https://welltory.com/plans/) · [Athlytic](https://www.corahealth.app/compare/athlytic) · [Google Health Coach](https://techcrunch.com/2026/05/07/googles-9-99-per-month-ai-health-coach-launches-may-19/) · [Google Health Premium](https://9to5google.com/2026/05/26/google-health-premium/) · [Apple Health+ scaled back](https://athletechnews.com/apple-is-scaling-back-its-ai-health-coach-plans-per-report/) · [Samsung Health 2026](https://news.samsung.com/global/samsung-introduces-next-gen-galaxy-watch-features-for-ai-powered-everyday-health-companion) · [Terra API Garmin](https://tryterra.co/integrations/garmin) · [Spike API MCP layer](https://www.spikeapi.com/mcp-health-ai-integration) · [Spike pricing](https://www.spikeapi.com/pricing)

**Nutrition:** [NIH accuracy study](https://www.sciencedaily.com/releases/2026/07/260726015237.htm) · [2026 accuracy literature review](https://platelens.app/blog/ai-calorie-counting-accuracy-research-2026) · [MyFitnessPal benchmark](https://ai-food-tracker.com/reviews/myfitnesspal/) · [Lose It! benchmark](https://ai-food-tracker.com/reviews/lose-it/) · [SnapCalorie CVPR results](https://www.snapcalorie.com/faq.html) · [ZOE photologging validation](https://zoe.com/learn/zoe-new-photologging-app) · [Cronometer nutrients](https://trackerbenchmark.com/reviews/cronometer/) · [Cronometer supplements](https://support.cronometer.com/hc/en-us/articles/360000328566-How-do-I-add-a-supplement-to-my-diary) · [MacroFactor Nutrient Explorer](https://macrofactor.com/micronutrients-nutrient-explorer/) · [MacroFactor pricing](https://hronikka.com/blog/macrofactor-pricing) · [Cal AI review](https://nutrola.app/en/blog/cal-ai-review-2026) · [Passio Nutrition-AI](https://passio.gitbook.io/nutrition-ai-hub) · [USDA FoodData Central API](https://fdc.nal.usda.gov/api-key-signup/) · [Nutritionix pricing](https://calorieapi.com/blog/nutritionix-api-pricing)

**EHR / FHIR:** [CMS Interoperability Final Rule](https://www.cms.gov/newsroom/fact-sheets/cms-interoperability-prior-authorization-final-rule-cms-0057-f) · [Info-blocking enforcement 2026](https://www.hklaw.com/en/insights/publications/2026/02/the-wait-is-over-information-blocking-enforcement-is-officially-here) · [TEFCA IAS / Epic MyChart Central](https://open.epic.com/Home/Interoperate/TEFCA/IAS) · [AHA IAS delay request](https://www.aha.org/lettercomment/2026-04-24-aha-comments-tefca-individual-access-procedure) · [Flexpa pricing](https://www.flexpa.com/pricing) · [Metriport](https://www.metriport.com/) · [Apple Health Records spec](https://support.apple.com/guide/healthregister/technical-requirements-specifications-health-apd12d144779/web) · [EHDS timeline](https://www.european-health-data-space.com/) · [Romania DES summer 2026](https://www.digi24.ro/digieconomic/financiar/dosarul-electronic-de-sanatate-devine-accesibil-din-vara-2026-acces-online-pentru-toti-pacientii-din-romania-94519) · [Moldova DES planning](https://www.moldpres.md/rom/societate/republica-moldova-va-implementa-sistemul-informational-dosarul-electronic-de-sanatate) · [Android FHIR SDK encryption](https://google.github.io/android-fhir/faq/) · [Medplum](https://www.medplum.com/open-source) · [HAPI FHIR](https://hapifhir.io/) · [fhir-kit-client](https://github.com/Vermonster/fhir-kit-client) · [Fasten Health](https://github.com/fastenhealth/fasten-onprem)

**Habits / dependency / coaching:** [Habit tracker comparison 2026](https://unstar.app/blog/habitica-streaks-productive-way-of-life-habit-bull-habit-tracker-apps-ranked-2026) · [Best habit trackers](https://2sync.com/blog/best-habit-tracker-apps) · [Notion Life OS templates](https://www.notion4management.com/blog/best-notion-life-os-templates) · [Sobriety app comparison 2026](https://sober-tracker.com/blog/best-sobriety-apps-2026-complete-comparison) · [I Am Sober review](https://www.choosingtherapy.com/i-am-sober-app-review/) · [Pear → PursueCare](https://www.statnews.com/2024/08/22/pear-pursuecare-reset-digital-therapeutic-substance-abuse/) · [Pear bankruptcy](https://www.fiercebiotech.com/medtech/cut-core-prescription-app-developer-pear-therapeutics-files-bankruptcy-lays-staff) · [Rocky.ai pricing](https://www.rocky.ai/app) · [Wysa review](https://www.choosingtherapy.com/wysa-app-review/) · [Youper shutdown](https://www.choosingtherapy.com/youper-app-review/) · [Ash / Slingshot AI](https://www.statnews.com/2025/07/22/slingshot-new-investors-generative-ai-mental-health-therapy-chatbot-called-ash/) · [BetterUp AI Coach](https://www.businesswire.com/news/home/20250121963707/en/BetterUp-Launches-AI-Coaching-Bridging-Human-Expertise-and-AI-Innovation-to-Transform-Organizational-Impact-at-Scale) · [InnerForge multi-coach](https://www.innerforge.ai/blog/best-ai-coaching-apps-alternative) · [Whoop Coach analysis](https://www.athletedata.health/guides/whoop-recovery-coach)

**Market:** [Health coaching market 2026–2030](https://www.globenewswire.com/news-release/2026/2/23/3242374/0/en/Health-Coaching-Industry-Report-2026-2035-A-35-5-Billion-Market-by-2030-Featuring-Health-Coach-Institute-Advanced-Wellness-Systems-Concentra-Noom-Wellcoaches-Among-Others.html) · [BVP State of Health AI 2026](https://www.bvp.com/atlas/state-of-health-ai-2026) · [AI therapy usage statistics](https://psychology.com/ai-therapy-statistics/)
