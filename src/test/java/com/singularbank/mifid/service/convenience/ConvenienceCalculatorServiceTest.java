package com.singularbank.mifid.service.convenience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.repository.CombinacionMatchingRepository;
import com.singularbank.mifid.repository.CombinacionRespuestaRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Convenience Calculator Service Tests")
class ConvenienceCalculatorServiceTest {

  private CombinacionRespuestaRepository combinacionRepository;
  private ConvenienceQuestionIdLoader questionIdLoader;
  private CombinacionMatchingRepository combinacionMatchingRepository;
  private ConvenienceCalculatorService calculator;

  // Constants for Answers
  private static final String ANSWER_A = "A"; // High level / Yes
  private static final String ANSWER_B = "B"; // Medium level / No
  private static final String ANSWER_C = "C"; // Low level
  private static final String ANSWER_D = "D"; // None / Correct for Q3, Q7

  // Question IDs
  private static final int Q1_EDUCATION = 1;
  private static final int Q2_PROFESSION = 2;
  private static final int Q3_GENERAL_KNOWLEDGE = 3;

  private static final int Q4_FAMILY_E = 4;
  private static final int Q5_FAMILY_F = 5;

  // Question 10 IDs (Family Experience)
  private static final int Q10_FAMILY_A = 24;
  private static final int Q10_FAMILY_E = 28;
  private static final int Q10_FAMILY_F = 29;

  @BeforeEach
  void setUp() {
    combinacionRepository = mock(CombinacionRespuestaRepository.class);
    questionIdLoader = mock(ConvenienceQuestionIdLoader.class);
    combinacionMatchingRepository = mock(CombinacionMatchingRepository.class);

    // Mock logic: Map order 11 -> ID 24, 12 -> 25, etc.
    lenient().when(questionIdLoader.getIdByOrder(anyShort(), anyShort()))
        .thenAnswer(inv -> {
          short order = inv.getArgument(1);
          return order + 13;
        });

    // Mock Combinacion ID lookup (Not critical for logic testing, but needed for object construction)
    lenient().when(combinacionRepository.findByFamilyCode(anyString(), anyBoolean()))
        .thenAnswer(inv -> {
          String familia = inv.getArgument(0);
          boolean conveniente = inv.getArgument(1);
          int baseId = conveniente ? 20 : 31;
          return baseId + (familia.charAt(0) - 'A');
        });

    calculator = new ConvenienceCalculatorService(combinacionRepository, questionIdLoader,
        combinacionMatchingRepository);
  }

  @Nested
  @DisplayName("Edge Cases & Basic Validation")
  class EdgeCases {

    @Test
    @DisplayName("Should return alert when no families evaluated")
    void noFamiliesEvaluated() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_A,
          Q2_PROFESSION, ANSWER_A,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D
      );

      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);

      assertThat(result.getConvenientFamilies()).isEmpty();
      assertThat(result.getAlerts()).anyMatch(s -> s.contains("No families"));
    }
  }

  @Nested
  @DisplayName("Complex Families (E-K)")
  class ComplexFamiliesTests {
    static Stream<Arguments> allConvenientCasesConveniencia() {
      return Stream.of(
              Arguments.of( "A","D","D",4,"C",28,"A",  "E"),
              Arguments.of( "A","D","D",4,"C",28,"B",  "E"),
              Arguments.of( "A","A","D",4,"C",28,"A",  "E"),
              Arguments.of( "A","A","D",4,"C",28,"B",  "E"),
              Arguments.of( "A","B","D",4,"C",28,"B",  "E"),
              Arguments.of( "A","C","D",4,"C",28,"A",  "E"),
              Arguments.of( "A","C","D",4,"C",28,"B",  "E"),
              Arguments.of( "B","D","D",4,"C",28,"A",  "E"),
              Arguments.of( "B","A","D",4,"C",28,"A",  "E"),
              Arguments.of( "B","B","D",4,"C",28,"A",  "E"),
              Arguments.of( "B","C","D",4,"C",28,"A",  "E"),
              Arguments.of( "C","D","D",4,"C",28,"A",  "E"),
              Arguments.of( "C","D","D",4,"C",28,"A",  "E"),
              Arguments.of( "C","A","D",4,"C",28,"A",  "E"),
              Arguments.of( "C","B","D",4,"C",28,"A",  "E"),
              Arguments.of( "C","C","D",4,"C",28,"A",  "E"),

              Arguments.of( "A","D","D",5,"B",29,"A",  "F"),
              Arguments.of( "A","D","D",5,"B",29,"B",  "F"),
              Arguments.of( "A","A","D",5,"B",29,"A",  "F"),
              Arguments.of( "A","A","D",5,"B",29,"B",  "F"),
              Arguments.of( "A","B","D",5,"B",29,"B",  "F"),
              Arguments.of( "A","C","D",5,"B",29,"A",  "F"),
              Arguments.of( "A","C","D",5,"B",29,"B",  "F"),
              Arguments.of( "B","D","D",5,"B",29,"A",  "F"),
              Arguments.of( "B","A","D",5,"B",29,"A",  "F"),
              Arguments.of( "B","B","D",5,"B",29,"A",  "F"),
              Arguments.of( "B","C","D",5,"B",29,"A",  "F"),
              Arguments.of( "C","D","D",5,"B",29,"A",  "F"),
              Arguments.of( "C","D","D",5,"B",29,"A",  "F"),
              Arguments.of( "C","A","D",5,"B",29,"A",  "F"),
              Arguments.of( "C","B","D",5,"B",29,"A",  "F"),
              Arguments.of( "C","C","D",5,"B",29,"A",  "F"),

              Arguments.of( "A","D","D",6,"B",30,"A",  "G"),
              Arguments.of( "A","D","D",6,"B",30,"B",  "G"),
              Arguments.of( "A","A","D",6,"B",30,"A",  "G"),
              Arguments.of( "A","A","D",6,"B",30,"B",  "G"),
              Arguments.of( "A","B","D",6,"B",30,"B",  "G"),
              Arguments.of( "A","C","D",6,"B",30,"A",  "G"),
              Arguments.of( "A","C","D",6,"B",30,"B",  "G"),
              Arguments.of( "B","D","D",6,"B",30,"A",  "G"),
              Arguments.of( "B","A","D",6,"B",30,"A",  "G"),
              Arguments.of( "B","B","D",6,"B",30,"A",  "G"),
              Arguments.of( "B","C","D",6,"B",30,"A",  "G"),
              Arguments.of( "C","D","D",6,"B",30,"A",  "G"),
              Arguments.of( "C","D","D",6,"B",30,"A",  "G"),
              Arguments.of( "C","A","D",6,"B",30,"A",  "G"),
              Arguments.of( "C","B","D",6,"B",30,"A",  "G"),
              Arguments.of( "C","C","D",6,"B",30,"A",  "G"),

              Arguments.of( "A","D","D",7,"D",31,"A",  "H"),
              Arguments.of( "A","D","D",7,"D",31,"B",  "H"),
              Arguments.of( "A","A","D",7,"D",31,"A",  "H"),
              Arguments.of( "A","A","D",7,"D",31,"B",  "H"),
              Arguments.of( "A","B","D",7,"D",31,"B",  "H"),
              Arguments.of( "A","C","D",7,"D",31,"A",  "H"),
              Arguments.of( "A","C","D",7,"D",31,"B",  "H"),
              Arguments.of( "B","D","D",7,"D",31,"A",  "H"),
              Arguments.of( "B","A","D",7,"D",31,"A",  "H"),
              Arguments.of( "B","B","D",7,"D",31,"A",  "H"),
              Arguments.of( "B","C","D",7,"D",31,"A",  "H"),
              Arguments.of( "C","D","D",7,"D",31,"A",  "H"),
              Arguments.of( "C","D","D",7,"D",31,"A",  "H"),
              Arguments.of( "C","A","D",7,"D",31,"A",  "H"),
              Arguments.of( "C","B","D",7,"D",31,"A",  "H"),
              Arguments.of( "C","C","D",7,"D",31,"A",  "H"),

              Arguments.of( "A","D","D",8,"B",32,"A",  "I"),
              Arguments.of( "A","D","D",8,"B",32,"B",  "I"),
              Arguments.of( "A","A","D",8,"B",32,"A",  "I"),
              Arguments.of( "A","A","D",8,"B",32,"B",  "I"),
              Arguments.of( "A","B","D",8,"B",32,"B",  "I"),
              Arguments.of( "A","C","D",8,"B",32,"A",  "I"),
              Arguments.of( "A","C","D",8,"B",32,"B",  "I"),
              Arguments.of( "B","D","D",8,"B",32,"A",  "I"),
              Arguments.of( "B","A","D",8,"B",32,"A",  "I"),
              Arguments.of( "B","B","D",8,"B",32,"A",  "I"),
              Arguments.of( "B","C","D",8,"B",32,"A",  "I"),
              Arguments.of( "C","D","D",8,"B",32,"A",  "I"),
              Arguments.of( "C","D","D",8,"B",32,"A",  "I"),
              Arguments.of( "C","A","D",8,"B",32,"A",  "I"),
              Arguments.of( "C","B","D",8,"B",32,"A",  "I"),
              Arguments.of( "C","C","D",8,"B",32,"A",  "I"),

              Arguments.of( "A","D","D",9,"C",33,"A",  "J"),
              Arguments.of( "A","D","D",9,"C",33,"B",  "J"),
              Arguments.of( "A","A","D",9,"C",33,"A",  "J"),
              Arguments.of( "A","A","D",9,"C",33,"B",  "J"),
              Arguments.of( "A","B","D",9,"C",33,"B",  "J"),
              Arguments.of( "A","C","D",9,"C",33,"A",  "J"),
              Arguments.of( "A","C","D",9,"C",33,"B",  "J"),
              Arguments.of( "B","D","D",9,"C",33,"A",  "J"),
              Arguments.of( "B","A","D",9,"C",33,"A",  "J"),
              Arguments.of( "B","B","D",9,"C",33,"A",  "J"),
              Arguments.of( "B","C","D",9,"C",33,"A",  "J"),
              Arguments.of( "C","D","D",9,"C",33,"A",  "J"),
              Arguments.of( "C","D","D",9,"C",33,"A",  "J"),
              Arguments.of( "C","A","D",9,"C",33,"A",  "J"),
              Arguments.of( "C","B","D",9,"C",33,"A",  "J"),
              Arguments.of( "C","C","D",9,"C",33,"A",  "J"),

              Arguments.of( "A","D","D",9,"C",34,"A",  "K"),
              Arguments.of( "A","D","D",9,"C",34,"B",  "K"),
              Arguments.of( "A","A","D",9,"C",34,"A",  "K"),
              Arguments.of( "A","A","D",9,"C",34,"B",  "K"),
              Arguments.of( "A","B","D",9,"C",34,"B",  "K"),
              Arguments.of( "A","C","D",9,"C",34,"A",  "K"),
              Arguments.of( "A","C","D",9,"C",34,"B",  "K"),
              Arguments.of( "B","D","D",9,"C",34,"A",  "K"),
              Arguments.of( "B","A","D",9,"C",34,"A",  "K"),
              Arguments.of( "B","B","D",9,"C",34,"A",  "K"),
              Arguments.of( "B","C","D",9,"C",34,"A",  "K"),
              Arguments.of( "C","D","D",9,"C",34,"A",  "K"),
              Arguments.of( "C","D","D",9,"C",34,"A",  "K"),
              Arguments.of( "C","A","D",9,"C",34,"A",  "K"),
              Arguments.of( "C","B","D",9,"C",34,"A",  "K"),
              Arguments.of( "C","C","D",9,"C",34,"A",  "K")
      );
    }
    @ParameterizedTest
    @MethodSource("allConvenientCasesConveniencia")
    void allComplexConvenient(String answerQ1, String answerQ2, String answerQ3, Integer Q_Family, String family,Integer Q10_Family, String answerQ10, String convenientFamily) {
      var responses = Map.of(
              Q1_EDUCATION, answerQ1,
              Q2_PROFESSION, answerQ2,
              Q3_GENERAL_KNOWLEDGE, answerQ3,
              Q_Family, family,
              Q10_Family, answerQ10
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getConvenientFamilies()).contains(convenientFamily);
    }

  }

  @Nested
  @DisplayName("Simple Families (A-D)")
  class SimpleFamiliesTests {

    @ParameterizedTest
    @CsvSource({
        "A, 24", "B, 25", "C, 26", "D, 27"
    })
    @DisplayName("Convenient with Tech Uni (A) and Experience (A)")
    void simpleFamiliesConvenient(String family, int q10Id) {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_A,
          Q2_PROFESSION, ANSWER_A,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          q10Id, ANSWER_A
      );

      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);

      assertThat(result.getConvenientFamilies()).contains(family);
    }


    static Stream<Arguments> allConvenientCasesConveniencia() {
      return Stream.of(
              Arguments.of("A","D","D","A"),
              Arguments.of("A","D","D","B"),
              Arguments.of("A","A","D","A"),
              Arguments.of("A","A","D","B"),
              Arguments.of("A","B","D","A"),
              Arguments.of("A","B","D","B"),
              Arguments.of("A","C","D","A"),
              Arguments.of("A","C","D","B"),

              Arguments.of("B","D","D","A"),

              Arguments.of("B","A","D","A"),
              Arguments.of("B","A","D","B"),

              Arguments.of("B","B","D","A"),
              Arguments.of("B","B","D","B"),

              Arguments.of("B","C","D","A"),
              Arguments.of("C","C","D","A"),
              Arguments.of("C","A","D","A"),
              Arguments.of("C","B","D","A"),
              Arguments.of("C","C","D","A"),
              Arguments.of("D","C","D","A")

      );
    }
    @ParameterizedTest
    @MethodSource("allConvenientCasesConveniencia")
    //Deberia ser igual para todas de las familias simples (A,B,C,D)
    @DisplayName("Convenient all cases")
    void simpleFamiliesAllCases(String answerQ1, String answerQ2, String answerQ3, String answerQ10) {
      var responses = Map.of(
              Q1_EDUCATION, answerQ1,
              Q2_PROFESSION, answerQ2,
              Q3_GENERAL_KNOWLEDGE, answerQ3,
              Q10_FAMILY_A, answerQ10
      );

      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
       assertThat(result.getConvenientFamilies()).contains("A");
    }
  }

  @Nested
  @DisplayName("Knowledge Validation (Q3 & Specifics)")
  class KnowledgeTests {

    @Test
    @DisplayName("Fails General Knowledge (Q3) -> Not Convenient (No Professional)")
    void failGeneralKnowledgeNoProf() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_C,
          Q2_PROFESSION, ANSWER_D,
          Q3_GENERAL_KNOWLEDGE, ANSWER_A, // Wrong (Correct is D)
          Q10_FAMILY_A, ANSWER_A
      );

      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);

      assertThat(result.getNotConvenientFamilies()).contains("A");
    }

    @Test
    @DisplayName("Fails General Knowledge (Q3) + Professional -> Incoherence Alert")
    void failGeneralKnowledgeWithProf() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_A,
          Q2_PROFESSION, ANSWER_A, // Professional
          Q3_GENERAL_KNOWLEDGE, ANSWER_A, // Wrong
          Q10_FAMILY_A, ANSWER_A
      );

      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);

      assertThat(result.getAlerts()).anyMatch(a -> a.contains("INCOHERENCE"));
      assertThat(result.getNotConvenientFamilies()).contains("A");
    }
  }

// ====================================================================================
// MATRIX TESTS BY EDUCATION LEVEL
// ====================================================================================

  @Nested
  @DisplayName("Matrix: Education A (Technical/Econ University)")
  class TechnicalUniversityMatrix {
    // Logic: Always Convenient if Knowledge Checks pass.

    @Test
    @DisplayName("Edu A + No Exp -> Convenient")
    void eduANoExp() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_A,
          Q2_PROFESSION, ANSWER_D,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_B // No Experience
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getConvenientFamilies()).contains("A");
    }
  }

  @Nested
  @DisplayName("Matrix: Education B (Other University)")
  class OtherUniversityMatrix {
    // Logic Non-Complex: Exp A -> OK. Exp B -> Needs Prof A or B.
    // Logic Complex: Exp A -> OK. Exp B -> Not Convenient (even if Prof A).

    @Test
    @DisplayName("Edu B + Exp A -> Convenient")
    void eduBWithExp() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_B,
          Q2_PROFESSION, ANSWER_D,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_A
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getConvenientFamilies()).contains("A");
    }

    @Test
    @DisplayName("Edu B + No Exp + High Prof (Non-Complex) -> Convenient")
    void eduBNoExpHighProf() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_B,
          Q2_PROFESSION, ANSWER_A, // High Prof
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_B   // No Exp
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getConvenientFamilies()).contains("A");
    }

    @Test
    @DisplayName("Edu B + No Exp + High Prof (Complex) -> Not Convenient")
    void eduBNoExpHighProfComplex() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_B,
          Q2_PROFESSION, ANSWER_A,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q5_FAMILY_F, ANSWER_B,   // Correct Specific Knowledge
          Q10_FAMILY_F, ANSWER_B   // No Exp
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getNotConvenientFamilies()).contains("F");
    }
  }

  @Nested
  @DisplayName("Matrix: Education C (Bachelor/FP) - CRITICAL FIX")
  class BachelorMatrix {
    // Logic: Strictly depends on Experience (P10). Profession is IGNORED.
    // Exp A -> Convenient.
    // Exp B -> Not Convenient (even with Prof A).

    @Test
    @DisplayName("Edu C + Exp A + No Prof -> Convenient")
    void eduCWithExpNoProf() {
      // Caso Fila 71 del Excel (Prof D, Exp A -> Conv)
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_C,
          Q2_PROFESSION, ANSWER_D,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_A // Has Experience
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getConvenientFamilies()).contains("A");
    }

    @Test
    @DisplayName("Edu C + Exp A + High Prof -> Convenient")
    void eduCWithExpHighProf() {
      // Caso Fila 77 del Excel (Prof A, Exp A -> Conv)
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_C,
          Q2_PROFESSION, ANSWER_A,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_A // Has Experience
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getConvenientFamilies()).contains("A");
    }

    @Test
    @DisplayName("Edu C + No Exp + High Prof -> Not Convenient (Prof Ignored)")
    void eduCNoExpHighProf() {
      // Caso Fila 78 del Excel (Prof A, Exp B -> No Conv)
      // Este es el caso crítico donde la profesión NO salva al cliente.
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_C,
          Q2_PROFESSION, ANSWER_A, // High Professional (Banker)
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_B   // No Experience
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);

      assertThat(result.getNotConvenientFamilies()).contains("A");
      assertThat(result.getConvenientFamilies()).doesNotContain("A");
    }

    @Test
    @DisplayName("Edu C + No Exp + No Prof -> Not Convenient")
    void eduCNoExpNoProf() {
      // Caso Fila 72 del Excel
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_C,
          Q2_PROFESSION, ANSWER_D,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_B
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);
      assertThat(result.getNotConvenientFamilies()).contains("A");
    }
  }

  @Nested
  @DisplayName("Matrix: Education D (No Higher Education)")
  class NoHigherEducationMatrix {
    // Logic Non-Complex: Exp A -> Convenient with ATYPICAL Alert.
    // Logic Complex: Always Not Convenient.

    @Test
    @DisplayName("Edu D + Exp A (Non-Complex) -> Convenient with Alert")
    void eduDWithExpNonComplex() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_D,
          Q2_PROFESSION, ANSWER_D,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q10_FAMILY_A, ANSWER_A
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);

      assertThat(result.getConvenientFamilies()).contains("A");
      assertThat(result.getAlerts()).anyMatch(a -> a.contains("ATYPICAL"));
    }

    @Test
    @DisplayName("Edu D + Exp A (Complex) -> Not Convenient")
    void eduDWithExpComplex() {
      var responses = Map.of(
          Q1_EDUCATION, ANSWER_D,
          Q2_PROFESSION, ANSWER_D,
          Q3_GENERAL_KNOWLEDGE, ANSWER_D,
          Q4_FAMILY_E, ANSWER_C, // Correct Specific
          Q10_FAMILY_E, ANSWER_A // Has Experience
      );
      ConvenienceResult result = calculator.calculateConvenienceDetailed(responses, (short) 1);

      assertThat(result.getNotConvenientFamilies()).contains("E");
    }
  }
}