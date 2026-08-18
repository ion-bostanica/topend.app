package app.topend.api.hello.application;

import app.topend.api.hello.application.port.in.HelloUseCase;
import app.topend.api.hello.application.port.out.ChatPort;
import app.topend.api.hello.domain.HelloResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class HelloService implements HelloUseCase {

	private final ChatPort chat;

	public HelloService(ChatPort chat) {
		this.chat = chat;
	}

	@Override
	public Mono<HelloResponse> hello(String name) {
		return chat.chat("Say hello to " + name + " in one short sentence.")
			.map(message -> new HelloResponse(chat.provider(), message));
	}

}
