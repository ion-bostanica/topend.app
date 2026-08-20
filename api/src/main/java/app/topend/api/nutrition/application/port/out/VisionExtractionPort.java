package app.topend.api.nutrition.application.port.out;

import app.topend.api.nutrition.domain.ConsumptionProposal;
import reactor.core.publisher.Mono;

public interface VisionExtractionPort {

	String EXTRACTION_PROMPT = """
			You are a nutrition extraction agent. Look at the meal photo and:
			1. Identify every distinct food and drink on it, estimating consumed quantity (g for solids, ml for liquids).
			2. For EACH identified food, call the searchFood tool to resolve it against USDA FoodData Central; \
			pick the best-matching candidate and use its fdcId. If nothing matches, leave fdcId null.
			3. Compute nutrient totals for the estimated quantity: FDC values are per 100 g/ml, \
			so total = value_per_100 * quantity / 100. Fill kcal, macros (fat with saturated/mono/poly, \
			carbs with sugars/fiber/starch, protein) and key micronutrients with their FDC unit.
			4. Set a 0..1 confidence per item and overall.""";

	Mono<ConsumptionProposal> extract(byte[] image, String mimeType);

	String provider();

}
