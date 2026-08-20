package app.topend.api.nutrition;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import app.topend.api.nutrition.adapter.in.web.NutritionController;
import app.topend.api.nutrition.application.port.in.ConsumptionUseCase;
import app.topend.api.nutrition.domain.Consumption;
import app.topend.api.nutrition.domain.ConsumptionProposal;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@WebFluxTest(NutritionController.class)
class NutritionControllerTest {

	@Autowired
	WebTestClient client;

	@MockitoBean
	ConsumptionUseCase consumptions;

	private static ConsumptionProposal.Item appleItem() {
		return new ConsumptionProposal.Item("apple", 1750340L, 182, "g", false, 107,
				new ConsumptionProposal.Macros(new ConsumptionProposal.Fat(0.3, 0, 0, 0),
						new ConsumptionProposal.Carbs(27, 21, 4.8, 0), 0.4),
				List.of(new ConsumptionProposal.Micro("iron", 0.04, "MG")), 0.95);
	}

	@Test
	void photoUploadReturnsProposal() {
		BDDMockito.given(this.consumptions.extractFromPhoto(BDDMockito.any(), BDDMockito.eq("image/jpeg")))
			.willReturn(Mono.just(new ConsumptionProposal(List.of(appleItem()), 0.9)));

		MultipartBodyBuilder body = new MultipartBodyBuilder();
		body.part("photo", new ByteArrayResource(new byte[] { 1, 2, 3 }) {
			@Override
			public String getFilename() {
				return "meal.jpg";
			}
		}).contentType(MediaType.IMAGE_JPEG);

		this.client.post()
			.uri("/nutrition/consumptions/extracts")
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(body.build()))
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.items[0].name")
			.isEqualTo("apple")
			.jsonPath("$.items[0].fdcId")
			.isEqualTo(1750340)
			.jsonPath("$.items[0].kcal")
			.isEqualTo(107)
			.jsonPath("$.confidence")
			.isEqualTo(0.9);
	}

	@Test
	void photoPartMissingIsBadRequest() {
		MultipartBodyBuilder body = new MultipartBodyBuilder();
		body.part("other", "not a photo");

		this.client.post()
			.uri("/nutrition/consumptions/extracts")
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(body.build()))
			.exchange()
			.expectStatus()
			.isBadRequest();
	}

	@Test
	void oversizedPhotoIsPayloadTooLarge() {
		MultipartBodyBuilder body = new MultipartBodyBuilder();
		body.part("photo", new ByteArrayResource(new byte[11 * 1024 * 1024]) {
			@Override
			public String getFilename() {
				return "huge.jpg";
			}
		}).contentType(MediaType.IMAGE_JPEG);

		this.client.post()
			.uri("/nutrition/consumptions/extracts")
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(body.build()))
			.exchange()
			.expectStatus()
			.isEqualTo(413);
	}

	@Test
	void jsonLogReturnsCreatedConsumption() {
		UUID id = UUID.randomUUID();
		BDDMockito.given(this.consumptions.log(BDDMockito.any()))
			.willReturn(Mono.just(new Consumption(id, Instant.parse("2026-08-19T12:00:00Z"), "photo", "ion",
					List.of(appleItem()))));

		this.client.post()
			.uri("/nutrition/consumptions")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("""
					{"consumedAt":"2026-08-19T12:00:00Z","source":"photo","confirmedBy":"ion",
					 "items":[{"name":"apple","fdcId":1750340,"quantity":182,"unit":"g","liquid":false,
					           "kcal":107,"macros":{"fat":{"totalG":0.3,"saturatedG":0,"monoG":0,"polyG":0},
					           "carbs":{"totalG":27,"sugarsG":21,"fiberG":4.8,"starchG":0},"proteinG":0.4},
					           "micros":[{"nutrientKey":"iron","amount":0.04,"unit":"MG"}],"confidence":0.95}]}
					""")
			.exchange()
			.expectStatus()
			.isCreated()
			.expectBody()
			.jsonPath("$.id")
			.isEqualTo(id.toString())
			.jsonPath("$.items[0].name")
			.isEqualTo("apple");
	}

	@Test
	void jsonLogWithoutItemsIsBadRequest() {
		this.client.post()
			.uri("/nutrition/consumptions")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"consumedAt\":\"2026-08-19T12:00:00Z\",\"source\":\"photo\",\"items\":[]}")
			.exchange()
			.expectStatus()
			.isBadRequest();
	}

}
