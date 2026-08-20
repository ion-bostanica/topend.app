package app.topend.api.nutrition;

import app.topend.api.nutrition.application.port.in.ConsumptionUseCase;
import app.topend.api.nutrition.domain.ConsumptionProposal;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live agentic-loop test: real vision model + real USDA FDC calls via the searchFood tool.
 * Photos are human-classified samples from Wikimedia Commons (a red apple; a bibimbap bowl).
 * Opt-in: only runs when FDC_API_KEY is exported; the AI key comes from the active
 * provider's config (switch providers with SPRING_AI_DEFAULT=anthropic|openai).
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "FDC_API_KEY", matches = ".+")
class ExtractionAgentIT {

	private static final Logger log = LoggerFactory.getLogger(ExtractionAgentIT.class);

	@Autowired
	ConsumptionUseCase consumptions;

	@ParameterizedTest
	@ValueSource(strings = { "photos/apple.jpg", "photos/meal.jpg" })
	void extractsFoodsFromKnownPhoto(String photo) throws Exception {
		byte[] image = new ClassPathResource(photo).getInputStream().readAllBytes();

		ConsumptionProposal proposal = this.consumptions.extractFromPhoto(image, "image/jpeg").block();

		log.info("Proposal for {}: {}", photo, proposal);
		assertThat(proposal).isNotNull();
		assertThat(proposal.items()).isNotEmpty();
		assertThat(proposal.items()).allSatisfy(item -> assertThat(item.name()).isNotBlank());
		// Foods the tool can't resolve stay fdcId=null with zero nutrients (FR-NUT-2: a human fixes them) —
		// but the photo must yield at least one resolved item with real energy data.
		assertThat(proposal.items()).anySatisfy(item -> {
			assertThat(item.fdcId()).isNotNull();
			assertThat(item.kcal()).isGreaterThan(0);
		});
		assertThat(proposal.confidence()).isBetween(0.0, 1.0);
	}

}
