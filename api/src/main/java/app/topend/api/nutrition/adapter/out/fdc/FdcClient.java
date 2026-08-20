package app.topend.api.nutrition.adapter.out.fdc;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin consumer of the USDA FoodData Central search API.
 * Nutrient values in results are per 100 g/ml (FDC convention).
 */
@Component
public class FdcClient {

	private static final int PAGE_SIZE = 5;

	private final RestClient restClient;

	private final String apiKey;

	public FdcClient(RestClient.Builder builder,
			@Value("${fdc.base-url:https://api.nal.usda.gov/fdc}") String baseUrl,
			@Value("${fdc.api-key:}") String apiKey) {
		this.restClient = builder.baseUrl(baseUrl).build();
		this.apiKey = apiKey;
	}

	public List<FoodSearchResult> search(String query) {
		return restClient.get()
			.uri(uri -> uri.path("/v1/foods/search")
				.queryParam("query", query)
				.queryParam("pageSize", PAGE_SIZE)
				.queryParam("api_key", apiKey)
				.build())
			.retrieve()
			.body(SearchResponse.class)
			.foods();
	}

	record SearchResponse(List<FoodSearchResult> foods) {
	}

	public record FoodSearchResult(long fdcId, String description, String dataType,
			List<FoodNutrient> foodNutrients) {
	}

	public record FoodNutrient(long nutrientId, String nutrientName, String unitName, double value) {
	}

}
