package app.topend.api.hello.application.port.in;

import app.topend.api.hello.domain.HelloResponse;
import reactor.core.publisher.Mono;

public interface HelloUseCase {

	Mono<HelloResponse> hello(String name);

}
