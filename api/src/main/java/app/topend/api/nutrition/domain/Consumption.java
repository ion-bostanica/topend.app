package app.topend.api.nutrition.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A confirmed consumption (FR-NUT-2: human confirmed the AI proposal or entered manually). */
public record Consumption(UUID id, Instant consumedAt, String source, String confirmedBy,
		List<ConsumptionProposal.Item> items) {
}
