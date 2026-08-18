package app.topend.api.hello;

import app.topend.api.hello.adapter.in.web.HelloController;
import app.topend.api.hello.application.port.in.HelloUseCase;
import app.topend.api.hello.domain.HelloResponse;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(HelloController.class)
class HelloControllerTest {

	@Autowired
	WebTestClient client;

	@MockitoBean
	HelloUseCase hello;

	@Test
	void returnsProviderAndMessage() {
		BDDMockito.given(this.hello.hello("Ion")).willReturn(Mono.just(new HelloResponse("anthropic", "Hi Ion!")));

		this.client.post()
			.uri("/hello")
			.bodyValue("{\"name\":\"Ion\"}")
			.header("Content-Type", "application/json")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.provider")
			.isEqualTo("anthropic")
			.jsonPath("$.message")
			.isEqualTo("Hi Ion!");
	}

	@Test
	void rejectsBlankName() {
		this.client.post()
			.uri("/hello")
			.bodyValue("{\"name\":\"  \"}")
			.header("Content-Type", "application/json")
			.exchange()
			.expectStatus()
			.isBadRequest();
	}

}
