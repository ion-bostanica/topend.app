package app.topend.api.nutrition.adapter.in.web;

import java.time.Instant;
import java.util.List;

import app.topend.api.nutrition.application.port.in.ConsumptionUseCase;
import app.topend.api.nutrition.domain.Consumption;
import app.topend.api.nutrition.domain.ConsumptionProposal;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
public class NutritionController {

	// Providers cap images anyway (Anthropic 5MB, OpenAI 20MB) — bigger uploads are rejected, never buffered
	static final int MAX_PHOTO_BYTES = 10 * 1024 * 1024;

	private final ConsumptionUseCase consumptions;

	public NutritionController(ConsumptionUseCase consumptions) {
		this.consumptions = consumptions;
	}

	@PostMapping(value = "/nutrition/consumptions/extracts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ConsumptionProposal> propose(@RequestPart("photo") FilePart photo) {
		String mimeType = photo.headers().getContentType() != null ? photo.headers().getContentType().toString()
				: MediaType.IMAGE_JPEG_VALUE;
		return DataBufferUtils.join(photo.content(), MAX_PHOTO_BYTES).map(buffer -> {
			byte[] bytes = new byte[buffer.readableByteCount()];
			buffer.read(bytes);
			DataBufferUtils.release(buffer);
			return bytes;
		}).flatMap(bytes -> this.consumptions.extractFromPhoto(bytes, mimeType));
	}

	@PostMapping(value = "/nutrition/consumptions", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Consumption> create(@RequestBody LogConsumptionRequest request) {
		if (request.items() == null || request.items().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "items must not be empty");
		}
		return this.consumptions.log(new Consumption(null, request.consumedAt(), request.source(),
				request.confirmedBy(), request.items()));
	}

	@ExceptionHandler(org.springframework.core.io.buffer.DataBufferLimitException.class)
	@ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
	void photoTooLarge() {
		// multipart limits in application.yaml cap a photo at 10MB — the AI providers reject larger images anyway
	}

	record LogConsumptionRequest(Instant consumedAt, String source, String confirmedBy,
			List<ConsumptionProposal.Item> items) {
	}

}
