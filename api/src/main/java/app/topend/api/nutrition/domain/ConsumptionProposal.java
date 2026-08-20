package app.topend.api.nutrition.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** AI-extracted breakdown of a meal photo. Amounts are totals for the stated quantity. */
public record ConsumptionProposal(
		@JsonPropertyDescription("One entry per distinct food or drink identified on the photo") List<Item> items,
		@JsonPropertyDescription("Overall confidence 0..1 that the photo was interpreted correctly") double confidence) {

	public record Item(
			@JsonPropertyDescription("Short food name") String name,
			@JsonPropertyDescription("USDA FoodData Central fdcId resolved via the searchFood tool; null if no match") Long fdcId,
			@JsonPropertyDescription("Estimated consumed quantity") double quantity,
			@JsonPropertyDescription("g for solids, ml for liquids") String unit,
			@JsonPropertyDescription("true for drinks and liquid foods") boolean liquid,
			@JsonPropertyDescription("Total kilocalories for the stated quantity") double kcal,
			Macros macros,
			@JsonPropertyDescription("Micronutrient totals for the stated quantity") List<Micro> micros,
			@JsonPropertyDescription("Confidence 0..1 for this item") double confidence) {
	}

	public record Micro(
			@JsonPropertyDescription("Nutrient slug, e.g. iron, vitamin_c") String nutrientKey,
			@JsonPropertyDescription("Total amount for the stated quantity") double amount,
			@JsonPropertyDescription("FDC unit name, e.g. MG, UG") String unit) {
	}

	public record Macros(Fat fat, Carbs carbs,
			@JsonPropertyDescription("Total protein in grams") double proteinG) {
	}

	public record Fat(double totalG, double saturatedG, double monoG, double polyG) {
	}

	public record Carbs(double totalG, double sugarsG, double fiberG, double starchG) {
	}

}
