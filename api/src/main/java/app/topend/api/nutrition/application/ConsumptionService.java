package app.topend.api.nutrition.application;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import app.topend.api.nutrition.application.port.in.ConsumptionUseCase;
import app.topend.api.nutrition.application.port.out.VisionExtractionPort;
import app.topend.api.nutrition.domain.Consumption;
import app.topend.api.nutrition.domain.ConsumptionProposal;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsumptionService implements ConsumptionUseCase {

	// ponytail: ConcurrentHashMap store, swap to a Mongo repository port once the DBML-approved model ships
	private final Map<UUID, Consumption> store = new ConcurrentHashMap<>();

	private final VisionExtractionPort extractionPort;

	public ConsumptionService(VisionExtractionPort extractionPort) {
		this.extractionPort = extractionPort;
	}

	@Override
	public Mono<ConsumptionProposal> extractFromPhoto(byte[] image, String mimeType) {
		return extractionPort.extract(image, mimeType);
	}

	@Override
	public Mono<Consumption> log(Consumption consumption) {
		Consumption saved = new Consumption(UUID.randomUUID(), consumption.consumedAt(), consumption.source(),
				consumption.confirmedBy(), consumption.items());
		store.put(saved.id(), saved);
		return Mono.just(saved);
	}

	public Collection<Consumption> findAll() {
		return store.values();
	}

}
