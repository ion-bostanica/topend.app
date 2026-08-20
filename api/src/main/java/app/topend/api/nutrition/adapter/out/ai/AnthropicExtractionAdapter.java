package app.topend.api.nutrition.adapter.out.ai;

import app.topend.api.nutrition.adapter.out.fdc.FoodSearchTool;
import app.topend.api.nutrition.application.port.out.VisionExtractionPort;
import app.topend.api.nutrition.domain.ConsumptionProposal;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@ConditionalOnProperty(name = "spring.ai.default", havingValue = "anthropic")
public class AnthropicExtractionAdapter implements VisionExtractionPort {

	private final ChatClient chatClient;

	private final FoodSearchTool foodSearchTool;

	public AnthropicExtractionAdapter(AnthropicChatModel chatModel, FoodSearchTool foodSearchTool) {
		this.chatClient = ChatClient.builder(chatModel).build();
		this.foodSearchTool = foodSearchTool;
	}

	@Override
	public Mono<ConsumptionProposal> extract(byte[] image, String mimeType) {
		// Spring AI runs the agentic loop internally: model -> tool_use -> execute -> feed back -> repeat until end_turn.
		// useProviderStructuredOutput() sends our record's JSON schema natively (Anthropic output_format / OpenAI json_schema).
		return Mono.fromCallable(() -> chatClient.prompt()
			.user(u -> u.text(EXTRACTION_PROMPT).media(MimeTypeUtils.parseMimeType(mimeType), new ByteArrayResource(image)))
			.tools(foodSearchTool)
			.call()
			.entity(ConsumptionProposal.class, params -> params.useProviderStructuredOutput()))
			.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public String provider() {
		return "anthropic";
	}

}
