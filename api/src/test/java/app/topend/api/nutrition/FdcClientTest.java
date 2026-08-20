package app.topend.api.nutrition;

import java.nio.charset.StandardCharsets;
import java.util.List;

import app.topend.api.nutrition.adapter.out.fdc.FdcClient;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class FdcClientTest {

	private final RestClient.Builder builder = RestClient.builder();

	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

	private final FdcClient client = new FdcClient(builder, "https://api.nal.usda.gov/fdc", "test-api-key");

	@Test
	void searchesFoodsAndMapsResults() throws Exception {
		String cannedJson = new String(new ClassPathResource("fdc/search-response.json").getInputStream().readAllBytes(),
				StandardCharsets.UTF_8);
		server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://api.nal.usda.gov/fdc/v1/foods/search")))
			.andExpect(method(GET))
			.andExpect(queryParam("query", "apple"))
			.andExpect(queryParam("pageSize", "5"))
			.andExpect(queryParam("api_key", "test-api-key"))
			.andRespond(withSuccess(cannedJson, MediaType.APPLICATION_JSON));

		List<FdcClient.FoodSearchResult> results = client.search("apple");

		server.verify();
		assertThat(results).hasSize(2);
		FdcClient.FoodSearchResult apple = results.getFirst();
		assertThat(apple.fdcId()).isEqualTo(1750340L);
		assertThat(apple.description()).isEqualTo("Apples, red delicious, with skin, raw");
		assertThat(apple.dataType()).isEqualTo("Foundation");
		assertThat(apple.foodNutrients()).extracting(FdcClient.FoodNutrient::nutrientName)
			.contains("Energy", "Iron, Fe");
		assertThat(apple.foodNutrients().getFirst().value()).isEqualTo(59.0);
	}

}
