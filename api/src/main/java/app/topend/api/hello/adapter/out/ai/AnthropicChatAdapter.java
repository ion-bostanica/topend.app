package app.topend.api.hello.adapter.out.ai;

import app.topend.api.hello.application.port.out.ChatPort;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@ConditionalOnProperty(name = "spring.ai.default", havingValue = "anthropic")
public class AnthropicChatAdapter implements ChatPort {

	private final AnthropicChatModel chatModel;

	public AnthropicChatAdapter(AnthropicChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public Mono<String> chat(String prompt) {
		return Mono.fromCallable(() -> chatModel.call(prompt)).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public String provider() {
		return "anthropic";
	}

}
