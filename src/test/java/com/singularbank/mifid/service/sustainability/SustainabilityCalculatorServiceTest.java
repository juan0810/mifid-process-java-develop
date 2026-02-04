package com.singularbank.mifid.service.sustainability;

import static org.assertj.core.api.Assertions.assertThat;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.AnswersTest.Option;
import com.singularbank.mifid.entity.AnswersTest.Question;
import com.singularbank.mifid.entity.CustomerActiveTests.SustainabilityPreferences;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Sustainability Calculator Service Tests")
class SustainabilityCalculatorServiceTest {

  private final SustainabilityCalculatorService calculator =
      new SustainabilityCalculatorService();

  private static final int Q1_WANTS_SUSTAINABILITY = 2;
  private static final int Q2_SUSTAINABLE_INVESTMENT = 4;
  private static final int Q3_EU_TAXONOMY = 5;
  private static final int Q4_PAI = 6;
  private static final int Q5_PORTFOLIO_PERCENTAGE = 7;

  private static final String NO_PREFERENCES =
      "El cliente no ha manifestado preferencias de sostenibilidad";
  private static final String ALTERNATIVE_FLEXIBLE =
      "El cliente ha manifestado su deseo de recibir siempre que sea posible una alternativa en inversión sostenible";

  @Nested
  @DisplayName("Edge Cases - Invalid Input")
  class EdgeCases {

    @Test
    @DisplayName("Should return default message when AnswersTest is null")
    void nullAnswersTest() {
      String result = calculator.calculateSustainabilityResult(null);
      assertThat(result).isEqualTo("Sin resultado disponible");

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(null);
      assertThatIsEmptyPreferences(prefs);
    }

    @Test
    @DisplayName("Should return default message when questions is null")
    void nullQuestions() {
      var answersTest = AnswersTest.builder().testId(1).questions(null).build();

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).isEqualTo("Sin resultado disponible");

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThatIsEmptyPreferences(prefs);
    }

    @Test
    @DisplayName("Should return default message when questions is empty")
    void emptyQuestions() {
      var answersTest = AnswersTest.builder().testId(1).questions(List.of()).build();

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).isEqualTo("Sin resultado disponible");

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThatIsEmptyPreferences(prefs);
    }

    @Test
    @DisplayName("Should handle question with null option")
    void nullOption() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestionWithNullOption(Q1_WANTS_SUSTAINABILITY),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains("5% de productos sostenibles");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Should handle option with null, empty or blank value")
    void nullOrBlankValue(String value) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestionWithValue(Q1_WANTS_SUSTAINABILITY, value),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains("5% de productos sostenibles");
    }
  }

  @Nested
  @DisplayName("Answer Value Extraction - All Paths")
  class AnswerValueExtraction {

    @ParameterizedTest
    @ValueSource(strings = {"b", "B", "  b  "})
    @DisplayName("Should extract value when present")
    void extractFromValue(String value) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestionWithValue(Q1_WANTS_SUSTAINABILITY, value),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains("5% de productos sostenibles");
    }

    @Test
    @DisplayName("Should extract from score when value is blank")
    void extractFromScore() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              Question.builder()
                  .id(Q4_PAI)
                  .option(Option.builder().value("  ").score(5).build())
                  .build(),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains("todas las Principales Incidencias Adversas de forma general");
    }

    @Test
    @DisplayName("Should ignore score when it is 0")
    void ignoreScoreZero() {
      var answersTest = createAnswersTest(
          List.of(
              Question.builder()
                  .id(Q1_WANTS_SUSTAINABILITY)
                  .option(Option.builder().score(0).text("B) Sí").build())
                  .build(),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains("5% de productos sostenibles");
    }

    @ParameterizedTest
    @ValueSource(strings = {"B) Sí", "A) No", "C) Desc", "B", "C", "  C  ", "Invalid text", "1",
        "AB", "123"})
    @DisplayName("Should parse text formats successfully or use defaults")
    void parseTextFormats(String text) {
      var answersTest = createAnswersTest(
          List.of(
              Question.builder()
                  .id(Q1_WANTS_SUSTAINABILITY)
                  .option(Option.builder().text(text).build())
                  .build(),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).isNotBlank();
    }
  }

  @Nested
  @DisplayName("No Preferences Scenarios")
  class NoPreferencesScenarios {

    @ParameterizedTest
    @CsvSource({
        "A, A",
        "A, B",
        "A, C",
        "A, D",
        "C, A",
        "C, B",
        "C, C",
        "C, D"
    })
    @DisplayName("Should return no preferences when client says NO or DELEGATES")
    void noPreferencesCombinations(String answer1, String answer5) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, answer1),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, answer5)
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).isEqualTo(NO_PREFERENCES);

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThatIsEmptyPreferences(prefs);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Z", "X", "E"})
    @DisplayName("Should return no preferences for unknown answer5 values when no other preferences")
    void unknownAnswer5Values(String unknownValue) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, unknownValue)
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).isEqualTo(NO_PREFERENCES);
    }
  }

  @Nested
  @DisplayName("Flexible Portfolio Scenarios (Answer D)")
  class FlexiblePortfolioScenarios {

    @Test
    @DisplayName("Should return flexible message when D without other preferences")
    void flexibleWithoutPreferences() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, "1"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "D")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).isEqualTo(ALTERNATIVE_FLEXIBLE);

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThat(prefs.getPercentInPortfolio()).isNull();
      assertThat(prefs.getPercentSustainableInvestment()).isNull();
      assertThat(prefs.getPercentEUTaxonomyAlignment()).isNull();
      assertThat(prefs.getGreenhouseGasPAI()).isFalse();
    }

    @Test
    @DisplayName("Should return flexible with PAI when D + social PAI")
    void flexibleWithSocialPAI() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, "4"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "D")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result)
          .contains(
              "El cliente ha manifestado su deseo de recibir alternativas en inversión sostenible")
          .contains("materias sociales y laborales");

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThat(prefs.getPercentInPortfolio()).isNull();
      assertThat(prefs.getSocialPAI()).isTrue();
    }

    @Test
    @DisplayName("Should return flexible with PAI when D + general PAI")
    void flexibleWithGeneralPAI() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, "5"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "D")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result)
          .contains(
              "El cliente ha manifestado su deseo de recibir alternativas en inversión sostenible")
          .contains("todas las Principales Incidencias Adversas de forma general");

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThat(prefs.getPercentInPortfolio()).isNull();
      assertThat(prefs.getGeneralPAI()).isTrue();
    }
  }

  @Nested
  @DisplayName("Portfolio Percentage Scenarios")
  class PortfolioPercentageScenarios {

    @ParameterizedTest
    @CsvSource({
        "A, 5",
        "B, 10",
        "C, 20"
    })
    @DisplayName("Should build result with different portfolio percentages")
    void differentMainPercentages(String answer5, String expectedPercentage) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, "1"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, answer5)
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains(
          "El cliente ha manifestado su deseo de integrar un " + expectedPercentage +
              "% de productos sostenibles"
      );
    }

    @Test
    @DisplayName("Should not include specific preferences when all answers are default")
    void onlyMainPercentage() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, "1"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result)
          .isEqualTo(
              "El cliente ha manifestado su deseo de integrar un 5% de productos sostenibles conforme a sus preferencias")
          .doesNotContain(
              "inversión sostenible conforme al reglamento de divulgación",
              "medioambientalmente sostenibles conforme al reglamento de taxonomía",
              "Principales Incidencias Adversas"
          );
    }
  }

  @Nested
  @DisplayName("Sustainable Investment Preference")
  class SustainableInvestmentScenarios {

    @ParameterizedTest
    @CsvSource({
        "B, 5",
        "C, 20"
    })
    @DisplayName("Should include sustainable investment with different percentages")
    void sustainableInvestmentPercentages(String answer2, String expectedPercentage) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, answer2),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, "1"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains(
          "Productos con una proporción mínima del " + expectedPercentage +
              "% de inversión sostenible conforme al reglamento de divulgación"
      );
    }

    @Test
    @DisplayName("Should not include sustainable investment when answer is A or unknown")
    void noSustainableInvestment() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).doesNotContain(
          "inversión sostenible conforme al reglamento de divulgación");
    }
  }

  @Nested
  @DisplayName("EU Taxonomy Alignment Preference")
  class EUTaxonomyScenarios {

    @ParameterizedTest
    @CsvSource({
        "B, 2",
        "C, 5"
    })
    @DisplayName("Should include EU taxonomy with different percentages")
    void taxonomyPercentages(String answer3, String expectedPercentage) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, answer3),
              createQuestion(Q4_PAI, "1"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains(
          "Productos con una proporción mínima del " + expectedPercentage +
              "% de inversiones medioambientalmente sostenibles conforme al reglamento de taxonomía"
      );
    }

    @Test
    @DisplayName("Should not include EU taxonomy when answer is A or unknown")
    void noEUTaxonomy() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q3_EU_TAXONOMY, "Z"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).doesNotContain(
          "medioambientalmente sostenibles conforme al reglamento de taxonomía");
    }
  }

  @Nested
  @DisplayName("Principal Adverse Impacts (PAI)")
  class AdverseImpactsScenarios {

    @ParameterizedTest
    @CsvSource({
        "2, relacionadas con la emisión de gases con efecto invernadero",
        "3, Medioambientales como en la biodiversidad",
        "4, en materias sociales y laborales",
        "5, todas las Principales Incidencias Adversas de forma general"
    })
    @DisplayName("Should include correct PAI text for different scores")
    void adverseImpactsForDifferentScores(String score, String expectedFragment) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, score),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains(expectedFragment);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "6", "Z", ""})
    @DisplayName("Should not include PAI when score is 1, unknown or invalid")
    void noPAI(String score) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q4_PAI, score),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).doesNotContain("Principales Incidencias Adversas");
    }
  }

  @Nested
  @DisplayName("Multiple Preferences - Comma Separation")
  class MultiplePreferencesScenarios {

    @Test
    @DisplayName("Should separate with colon when adding first preference")
    void firstPreference() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "B"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains(": Productos con una proporción mínima del 5%");
    }

    @Test
    @DisplayName("Should separate second preference with comma")
    void secondPreference() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "B"),
              createQuestion(Q3_EU_TAXONOMY, "B"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      String[] parts = result.split(": ")[1].split(", ");
      assertThat(parts).hasSize(2);
    }

    @Test
    @DisplayName("Should separate third preference with comma")
    void thirdPreference() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "B"),
              createQuestion(Q3_EU_TAXONOMY, "B"),
              createQuestion(Q4_PAI, "5"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      String[] parts = result.split(": ")[1].split(", ");
      assertThat(parts).hasSize(3);
    }

    @Test
    @DisplayName("Should build complete result with all preferences")
    void allPreferences() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "C"),
              createQuestion(Q3_EU_TAXONOMY, "C"),
              createQuestion(Q4_PAI, "5"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "C")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result)
          .contains("20% de productos sostenibles")
          .contains("20% de inversión sostenible")
          .contains("5% de inversiones medioambientalmente sostenibles")
          .contains("todas las Principales Incidencias Adversas de forma general");
    }
  }

  @Nested
  @DisplayName("Sustainability Preferences Object Extraction")
  class PreferencesObjectTests {

    @ParameterizedTest
    @CsvSource({
        "A, A",
        "A, B",
        "C, A",
        "C, D"
    })
    @DisplayName("Should extract empty preferences when conditions met")
    void emptyPreferences(String answer1, String answer5) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, answer1),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, answer5)
          )
      );

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThatIsEmptyPreferences(prefs);
    }

    @Test
    @DisplayName("Should extract complete preferences with all values")
    void completePreferences() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "C"),
              createQuestion(Q3_EU_TAXONOMY, "B"),
              createQuestion(Q4_PAI, "3"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "B")
          )
      );

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);

      assertThat(prefs.getPercentInPortfolio()).isEqualTo(10);
      assertThat(prefs.getPercentSustainableInvestment()).isEqualTo(20);
      assertThat(prefs.getPercentEUTaxonomyAlignment()).isEqualTo(2);
      assertThat(prefs.getGreenhouseGasPAI()).isFalse();
      assertThat(prefs.getEnvironmentalPAI()).isTrue();
      assertThat(prefs.getSocialPAI()).isFalse();
      assertThat(prefs.getGeneralPAI()).isFalse();
    }

    @Test
    @DisplayName("Should extract preferences with nulls for default answers")
    void preferencesWithNulls() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, "A"),
              createQuestion(Q3_EU_TAXONOMY, "A"),
              createQuestion(Q4_PAI, "1"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);

      assertThat(prefs.getPercentInPortfolio()).isEqualTo(5);
      assertThat(prefs.getPercentSustainableInvestment()).isNull();
      assertThat(prefs.getPercentEUTaxonomyAlignment()).isNull();
      assertThat(prefs.getGreenhouseGasPAI()).isFalse();
      assertThat(prefs.getEnvironmentalPAI()).isFalse();
      assertThat(prefs.getSocialPAI()).isFalse();
      assertThat(prefs.getGeneralPAI()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "2, true, false, false, false",
        "3, false, true, false, false",
        "4, false, false, true, false",
        "5, false, false, false, true"
    })
    @DisplayName("Should set correct PAI flags for each type")
    void paiFlags(String score, boolean greenhouse, boolean environmental,
        boolean social, boolean general) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q4_PAI, score),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);

      assertThat(prefs.getGreenhouseGasPAI()).isEqualTo(greenhouse);
      assertThat(prefs.getEnvironmentalPAI()).isEqualTo(environmental);
      assertThat(prefs.getSocialPAI()).isEqualTo(social);
      assertThat(prefs.getGeneralPAI()).isEqualTo(general);
    }

    @ParameterizedTest
    @CsvSource({
        "A, 5",
        "B, 10",
        "C, 20",
        "D,",
        "Z,"
    })
    @DisplayName("Should parse portfolio percentage correctly")
    void portfolioPercentageParsing(String answer, Integer expected) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, answer)
          )
      );

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThat(prefs.getPercentInPortfolio()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "A,",
        "B, 5",
        "C, 20",
        "Z,"
    })
    @DisplayName("Should parse sustainable investment percentage correctly")
    void sustainableInvestmentParsing(String answer, Integer expected) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q2_SUSTAINABLE_INVESTMENT, answer),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThat(prefs.getPercentSustainableInvestment()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "A,",
        "B, 2",
        "C, 5",
        "Z,"
    })
    @DisplayName("Should parse EU taxonomy percentage correctly")
    void euTaxonomyParsing(String answer, Integer expected) {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q3_EU_TAXONOMY, answer),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      SustainabilityPreferences prefs = calculator.extractSustainabilityPreferences(answersTest);
      assertThat(prefs.getPercentEUTaxonomyAlignment()).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("Miscellaneous Scenarios")
  class MiscellaneousScenarios {

    @Test
    @DisplayName("Should handle duplicate question IDs keeping first value")
    void duplicateQuestionIds() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q1_WANTS_SUSTAINABILITY, "A"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result).contains("5% de productos sostenibles");
    }

    @Test
    @DisplayName("Should handle missing questions using defaults")
    void missingQuestions() {
      var answersTest = createAnswersTest(
          List.of(
              createQuestion(Q1_WANTS_SUSTAINABILITY, "B"),
              createQuestion(Q5_PORTFOLIO_PERCENTAGE, "A")
          )
      );

      String result = calculator.calculateSustainabilityResult(answersTest);
      assertThat(result)
          .contains("5% de productos sostenibles")
          .doesNotContain("inversión sostenible")
          .doesNotContain("Principales Incidencias Adversas");
    }
  }

  private AnswersTest createAnswersTest(List<Question> questions) {
    return AnswersTest.builder().testId(1).questions(questions).build();
  }

  private Question createQuestion(int id, String value) {
    return Question.builder()
        .id(id)
        .option(Option.builder().value(value).build())
        .build();
  }

  private Question createQuestionWithValue(int id, String value) {
    return Question.builder()
        .id(id)
        .option(Option.builder().value(value).build())
        .build();
  }

  private Question createQuestionWithNullOption(int id) {
    return Question.builder().id(id).option(null).build();
  }

  private void assertThatIsEmptyPreferences(SustainabilityPreferences prefs) {
    assertThat(prefs.getPercentInPortfolio()).isNull();
    assertThat(prefs.getPercentSustainableInvestment()).isNull();
    assertThat(prefs.getPercentEUTaxonomyAlignment()).isNull();
    assertThat(prefs.getGreenhouseGasPAI()).isFalse();
    assertThat(prefs.getEnvironmentalPAI()).isFalse();
    assertThat(prefs.getSocialPAI()).isFalse();
    assertThat(prefs.getGeneralPAI()).isFalse();
  }
}