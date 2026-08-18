package app.topend.api.hello.adapter.in.web;

import app.topend.api.hello.application.port.in.HelloUseCase;
import app.topend.api.hello.domain.HelloResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

	private final HelloUseCase hello;

	public HelloController(HelloUseCase hello) {
		this.hello = hello;
	}

	public record HelloRequest(String name) {
	}

	@PostMapping("/hello")
	public Mono<HelloResponse> hello(@RequestBody HelloRequest request) {
		if (request.name() == null || request.name().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
		}
		return hello.hello(request.name());
	}

}
