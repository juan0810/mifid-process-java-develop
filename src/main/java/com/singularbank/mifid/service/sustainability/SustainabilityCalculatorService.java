package com.singularbank.mifid.service.sustainability;

import com.singularbank.mifid.entity.AnswersTest;
import com.singularbank.mifid.entity.CustomerActiveTests.SustainabilityPreferences;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SustainabilityCalculatorService {

    private static final String NO_PREFERENCES =
            "El cliente no ha manifestado preferencias de sostenibilidad";
    private static final String UNAVAILABLE_RESULT =
            "Sin resultado disponible";
    private static final String PREFIX =
            "El cliente ha manifestado su deseo de integrar un %d%% de productos sostenibles conforme a sus preferencias";
    private static final String ALTERNATIVE_WITH_PREFERENCES =
            "El cliente ha manifestado su deseo de recibir alternativas en inversión sostenible conforme a sus preferencias: %s";
    private static final String ALTERNATIVE_WITHOUT_PREFERENCES =
            "El cliente ha manifestado su deseo de recibir siempre que sea posible una alternativa en inversión sostenible";
    private static final String CUSTOM_PREFERENCES =
            "El cliente ha manifestado su deseo de integrar productos sostenibles conforme a sus preferencias: %s";

    private static final String SUSTAINABLE_INVESTMENT_TEXT =
            "Productos con una proporción mínima del %d%% de inversión sostenible conforme al reglamento de divulgación";
    private static final String EU_TAXONOMY_TEXT =
            "Productos con una proporción mínima del %d%% de inversiones medioambientalmente sostenibles conforme al reglamento de taxonomía";
    private static final String PAI_GREENHOUSE =
            "Productos que tengan en cuenta las Principales Incidencias Adversas relacionadas con la emisión de gases con efecto invernadero";
    private static final String PAI_ENVIRONMENTAL =
            "Productos que tengan en cuenta las Principales Incidencias Adversas Medioambientales como en la biodiversidad, daños del agua o vertidos de agua o residuos";
    private static final String PAI_SOCIAL =
            "Productos que tengan en cuenta las Principales Incidencias Adversas en materias sociales y laborales, respeto a los derechos humanos y la lucha contra la corrupción y el soborno";
    private static final String PAI_GENERAL =
            "Productos que tengan en cuenta todas las Principales Incidencias Adversas de forma general";

    public String calculateSustainabilityResult(AnswersTest answersTest) {
        if (isInvalidTest(answersTest)) {
            return UNAVAILABLE_RESULT;
        }

        var answersMap = buildAnswersMap(answersTest);
        log.debug("Processing sustainability test {} with {} answers",
                answersTest.getTestId(), answersMap.size());

        return calculateResult(answersMap);
    }

    public SustainabilityPreferences extractSustainabilityPreferences(AnswersTest answersTest) {
        if (isInvalidTest(answersTest)) {
            return buildEmptyPreferences();
        }

        var answersMap = buildAnswersMap(answersTest);
        return buildPreferences(answersMap);
    }

    private boolean isInvalidTest(AnswersTest answersTest) {
        return answersTest == null
                || answersTest.getQuestions() == null
                || answersTest.getQuestions().isEmpty();
    }

    private Map<Integer, String> buildAnswersMap(AnswersTest answersTest) {
        return answersTest.getQuestions().stream()
                .collect(Collectors.toMap(
                        AnswersTest.Question::getId,
                        this::extractAnswerValue,
                        (existing, replacement) -> existing
                ));
    }

    private String extractAnswerValue(AnswersTest.Question question) {
        var option = question.getOption();
        if (option == null) {
            return "";
        }

        if (option.getValue() != null && !option.getValue().isBlank()) {
            return option.getValue().trim().toUpperCase();
        }

        if (option.getScore() != null && option.getScore() != 0) {
            return String.valueOf(option.getScore());
        }

        return parseAnswerFromText(option.getText());
    }

    private String parseAnswerFromText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        if (text.length() >= 2 && Character.isLetter(text.charAt(0)) && text.charAt(1) == ')') {
            return String.valueOf(text.charAt(0)).toUpperCase();
        }

        var trimmed = text.trim();
        if (trimmed.length() == 1 && Character.isLetter(trimmed.charAt(0))) {
            return trimmed.toUpperCase();
        }

        return "";
    }

    private String calculateResult(Map<Integer, String> answers) {
        var wantsSustainability = answers.getOrDefault(QuestionIds.WANTS_SUSTAINABILITY, "");
        if (SustainabilityChoice.hasNoPreferences(wantsSustainability)) {
            return NO_PREFERENCES;
        }

        var portfolioPercentage = answers.getOrDefault(QuestionIds.PORTFOLIO_PERCENTAGE, "");
        var mainPercentage = PortfolioPercentage.getPercentage(portfolioPercentage);
        var preferencesList = collectPreferences(answers);

        if (mainPercentage != null) {
            return formatResultWithPercentage(mainPercentage, preferencesList);
        }

        if (PortfolioPercentage.isFlexible(portfolioPercentage)) {
            return formatFlexibleResult(preferencesList);
        }

        if (!preferencesList.isEmpty()) {
            return CUSTOM_PREFERENCES.formatted(String.join(", ", preferencesList));
        }
        return NO_PREFERENCES;
    }

    private List<String> collectPreferences(Map<Integer, String> answers) {
        var preferences = new ArrayList<String>();
        collectSustainableInvestmentPreference(answers, preferences);
        collectEUTaxonomyPreference(answers, preferences);
        collectPAIPreference(answers, preferences);
        return preferences;
    }

    private void collectSustainableInvestmentPreference(Map<Integer, String> answers,
                                                        List<String> preferences) {
        var answer = answers.getOrDefault(QuestionIds.SUSTAINABLE_INVESTMENT, "A");
        var percentage = switch (answer) {
            case "B" -> 5;
            case "C" -> 20;
            default -> null;
        };

        if (percentage != null) {
            preferences.add(SUSTAINABLE_INVESTMENT_TEXT.formatted(percentage));
        }
    }

    private void collectEUTaxonomyPreference(Map<Integer, String> answers,
                                             List<String> preferences) {
        var answer = answers.getOrDefault(QuestionIds.EU_TAXONOMY, "A");
        var percentage = switch (answer) {
            case "B" -> 2;
            case "C" -> 5;
            default -> null;
        };

        if (percentage != null) {
            preferences.add(EU_TAXONOMY_TEXT.formatted(percentage));
        }
    }

    private void collectPAIPreference(Map<Integer, String> answers, List<String> preferences) {
        var answer = answers.getOrDefault(QuestionIds.PAI, "1");
        var paiText = PAIType.getText(answer);

        if (paiText != null) {
            preferences.add(paiText);
        }
    }

    private String formatResultWithPercentage(Integer percentage, List<String> preferences) {
        if (preferences.isEmpty()) {
            return PREFIX.formatted(percentage);
        }
        return PREFIX.formatted(percentage) + ": " + String.join(", ", preferences);
    }

    private String formatFlexibleResult(List<String> preferences) {
        if (preferences.isEmpty()) {
            return ALTERNATIVE_WITHOUT_PREFERENCES;
        }
        return ALTERNATIVE_WITH_PREFERENCES.formatted(String.join(", ", preferences));
    }

    private SustainabilityPreferences buildPreferences(Map<Integer, String> answers) {
        var wantsSustainability = answers.getOrDefault(QuestionIds.WANTS_SUSTAINABILITY, "");

        if (SustainabilityChoice.hasNoPreferences(wantsSustainability)) {
            return buildEmptyPreferences();
        }

        var portfolioPercentage = answers.getOrDefault(QuestionIds.PORTFOLIO_PERCENTAGE, "");
        var paiAnswer = answers.getOrDefault(QuestionIds.PAI, "1");

        return SustainabilityPreferences.builder()
                .percentInPortfolio(PortfolioPercentage.getPercentage(portfolioPercentage))
                .percentSustainableInvestment(
                        parseSustainableInvestmentPercentage(
                                answers.getOrDefault(QuestionIds.SUSTAINABLE_INVESTMENT, "A")))
                .percentEUTaxonomyAlignment(
                        parseEUTaxonomyPercentage(
                                answers.getOrDefault(QuestionIds.EU_TAXONOMY, "A")))
                .greenhouseGasPAI(PAIType.matches(paiAnswer, "2"))
                .environmentalPAI(PAIType.matches(paiAnswer, "3"))
                .socialPAI(PAIType.matches(paiAnswer, "4"))
                .generalPAI(PAIType.matches(paiAnswer, "5"))
                .build();
    }

    private Integer parseSustainableInvestmentPercentage(String answer) {
        return switch (answer) {
            case "B" -> 5;
            case "C" -> 20;
            default -> null;
        };
    }

    private Integer parseEUTaxonomyPercentage(String answer) {
        return switch (answer) {
            case "B" -> 2;
            case "C" -> 5;
            default -> null;
        };
    }

    private SustainabilityPreferences buildEmptyPreferences() {
        return SustainabilityPreferences.builder()
                .percentInPortfolio(null)
                .percentSustainableInvestment(null)
                .percentEUTaxonomyAlignment(null)
                .greenhouseGasPAI(false)
                .environmentalPAI(false)
                .socialPAI(false)
                .generalPAI(false)
                .build();
    }

    private static final class QuestionIds {
        static final int WANTS_SUSTAINABILITY = 17;
        static final int SUSTAINABLE_INVESTMENT = 18;
        static final int EU_TAXONOMY = 19;
        static final int PAI = 20;
        static final int PORTFOLIO_PERCENTAGE = 21;

        private QuestionIds() {
        }
    }

    private enum SustainabilityChoice {
        NO("A"),
        YES("B"),
        DELEGATE_TO_ADVISOR("C");

        private final String code;

        SustainabilityChoice(String code) {
            this.code = code;
        }

        static boolean hasNoPreferences(String code) {
            return NO.code.equals(code) || DELEGATE_TO_ADVISOR.code.equals(code);
        }
    }

    private enum PortfolioPercentage {
        FIVE_PERCENT("A", 5),
        TEN_PERCENT("B", 10),
        TWENTY_PERCENT("C", 20),
        FLEXIBLE("D", null);

        private final String code;
        private final Integer percentage;

        PortfolioPercentage(String code, Integer percentage) {
            this.code = code;
            this.percentage = percentage;
        }

        static Integer getPercentage(String code) {
            for (var value : values()) {
                if (value.code.equals(code)) {
                    return value.percentage;
                }
            }
            return null;
        }

        static boolean isFlexible(String code) {
            return FLEXIBLE.code.equals(code);
        }
    }

    private enum PAIType {
        NONE("1", null),
        GREENHOUSE("2", PAI_GREENHOUSE),
        ENVIRONMENTAL("3", PAI_ENVIRONMENTAL),
        SOCIAL("4", PAI_SOCIAL),
        GENERAL("5", PAI_GENERAL);

        private final String code;
        private final String text;

        PAIType(String code, String text) {
            this.code = code;
            this.text = text;
        }

        static String getText(String code) {
            for (var value : values()) {
                if (value.code.equals(code)) {
                    return value.text;
                }
            }
            return null;
        }

        static boolean matches(String actual, String expected) {
            return expected.equals(actual);
        }
    }
}