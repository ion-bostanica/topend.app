package app.topend.api.nutrition.application.port.in;

import app.topend.api.nutrition.domain.Consumption;
import app.topend.api.nutrition.domain.ConsumptionProposal;
import reactor.core.publisher.Mono;

public interface ConsumptionUseCase {

	Mono<ConsumptionProposal> extractFromPhoto(byte[] image, String mimeType);

	Mono<Consumption> log(Consumption consumption);

}
