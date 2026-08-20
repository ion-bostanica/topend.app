package app.topend.api.nutrition;

import java.time.Instant;
import java.util.List;

import app.topend.api.nutrition.application.ConsumptionService;
import app.topend.api.nutrition.application.port.out.VisionExtractionPort;
import app.topend.api.nutrition.domain.Consumption;
import app.topend.api.nutrition.domain.ConsumptionProposal;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumptionServiceTest {

	private final VisionExtractionPort extractionPort = Mockito.mock(VisionExtractionPort.class);

	private final ConsumptionService service = new ConsumptionService(extractionPort);

	@Test
	void extractFromPhotoDelegatesToPort() {
		ConsumptionProposal proposal = new ConsumptionProposal(List.of(), 0.9);
		BDDMockito.given(extractionPort.extract(BDDMockito.any(), BDDMockito.eq("image/jpeg")))
			.willReturn(Mono.just(proposal));

		ConsumptionProposal result = service.extractFromPhoto(new byte[] { 1 }, "image/jpeg").block();

		assertThat(result).isSameAs(proposal);
	}

	@Test
	void logAssignsIdAndStores() {
		Consumption unsaved = new Consumption(null, Instant.parse("2026-08-19T12:00:00Z"), "photo", "ion",
				List.of(new ConsumptionProposal.Item("apple", 1750340L, 182, "g", false, 107,
						new ConsumptionProposal.Macros(new ConsumptionProposal.Fat(0.3, 0, 0, 0),
								new ConsumptionProposal.Carbs(27, 21, 4.8, 0), 0.4),
						List.of(new ConsumptionProposal.Micro("iron", 0.04, "MG")), 0.95)));

		Consumption saved = service.log(unsaved).block();

		assertThat(saved.id()).isNotNull();
		assertThat(saved.items()).hasSize(1);
		assertThat(service.findAll()).containsExactly(saved);
	}

}
