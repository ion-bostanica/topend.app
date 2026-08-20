package app.topend.api.nutrition.adapter.out.fdc;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * The food-search tool the model calls during extraction. Spring AI turns this
 * into an Anthropic tool / OpenAI function definition — same code, both providers.
 */
@Component
public class FoodSearchTool {

	private final FdcClient fdcClient;

	public FoodSearchTool(FdcClient fdcClient) {
		this.fdcClient = fdcClient;
	}

	@Tool(description = "Search USDA FoodData Central for a food by name. Returns candidate foods with "
			+ "their FDC id and nutrient values per 100 g/ml (energy KCAL, protein, fat, carbs, micronutrients).")
	public List<FdcClient.FoodSearchResult> searchFood(
			@ToolParam(description = "Plain food name, e.g. 'red apple' or 'grilled chicken breast'") String query) {
		return fdcClient.search(query);
	}

}
