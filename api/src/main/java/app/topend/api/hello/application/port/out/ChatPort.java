package app.topend.api.hello.application.port.out;

import reactor.core.publisher.Mono;

public interface ChatPort {

	Mono<String> chat(String prompt);

	String provider();

}
