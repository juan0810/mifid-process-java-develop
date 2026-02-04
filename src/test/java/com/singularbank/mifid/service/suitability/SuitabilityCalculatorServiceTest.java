package com.singularbank.mifid.service.suitability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.singularbank.mifid.entity.SuitabilityProfile;
import com.singularbank.mifid.entity.SuitabilityResult;
import com.singularbank.mifid.repository.CombinacionRespuestaRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Suitability Calculator Service Tests")
class SuitabilityCalculatorServiceTest {

  private SuitabilityCalculatorService calculator;

  // IDONEIDAD (SuitabilityOrder)
  private static final int S_Q11_COMMITMENTS = 1;
  private static final int S_Q12_WEALTH_BANK = 3;
  private static final int S_Q12_WEALTH_TOTAL = 4;
  private static final int S_Q13_HORIZON = 5;
  private static final int S_Q14_LIQUIDITY = 6;
  private static final int S_Q15_PURPOSE = 7;
  private static final int S_Q16_LOSS_1MONTH = 9;
  private static final int S_Q16_LOSS_1YEAR = 10;
  private static final int S_Q16_LOSS_3YEARS = 11;

  // CONVENIENCIA (ConvenienceOrder)
  private static final int C_Q1_EDUCATION = 1;
  private static final int C_Q2_PROFESSION = 2;
  private static final int C_Q3_KNOWLEDGE = 3;
// NOTA: Q4 y Q5 en el modelo nuevo son preguntas técnicas de producto,
// no de Horizonte/Pérdidas, por lo que no se usan para validaciones cruzadas.

  private static final int C_Q10_BASE = 11; // Familia A

  private static final String ANSWER_A = "A";
  private static final String ANSWER_B = "B";
  private static final String ANSWER_C = "C";
  private static final String ANSWER_D = "D";
  private static final String ANSWER_E = "E";
  private static final String TWO_OR_MORE = "A"; // Para Q10 (Dos o más)

  @BeforeEach
  void setUp() {
    CombinacionRespuestaRepository combinacionRepository = mock(
        CombinacionRespuestaRepository.class);
    // Mockeamos IDs ficticios para el test unitario
    lenient().when(combinacionRepository.findByProfileName("Conservador")).thenReturn(1);
    lenient().when(combinacionRepository.findByProfileName("Moderado")).thenReturn(2);
    lenient().when(combinacionRepository.findByProfileName("Flexible"))
        .thenReturn(3); // Excel dice "Flexible" para perfil 3
    lenient().when(combinacionRepository.findByProfileName("Decidido")).thenReturn(4);
    lenient().when(combinacionRepository.findByProfileName("Agresivo")).thenReturn(5);

    calculator = new SuitabilityCalculatorService(combinacionRepository);
  }

  private Map<Integer, String> createBaseSuitability() {
    return new HashMap<>(Map.of(
        S_Q11_COMMITMENTS, ANSWER_C,
        S_Q12_WEALTH_BANK, ANSWER_B,
        S_Q12_WEALTH_TOTAL, ANSWER_B,
        S_Q13_HORIZON, ANSWER_C,
        S_Q14_LIQUIDITY, ANSWER_C,
        S_Q15_PURPOSE, ANSWER_C,
        S_Q16_LOSS_1MONTH, ANSWER_B,
        S_Q16_LOSS_1YEAR, ANSWER_B,
        S_Q16_LOSS_3YEARS, ANSWER_B
    ));
  }

  private Map<Integer, String> createBaseConvenience() {
    return new HashMap<>(Map.of(
        C_Q1_EDUCATION, ANSWER_B,
        C_Q2_PROFESSION, ANSWER_D,
        C_Q3_KNOWLEDGE, ANSWER_D // Correcta
    ));
  }

  @Nested
  @DisplayName("Profile Calculation Tests")
  class ProfileCalculationTests {

    @Test
    @DisplayName("Profile: Conservative (Low risk, low knowledge)")
    void testConservativeProfile() {
      // Given
      Map<Integer, String> suitability = new HashMap<>();
      suitability.put(S_Q11_COMMITMENTS, ANSWER_A);
      suitability.put(S_Q12_WEALTH_BANK, ANSWER_A);
      suitability.put(S_Q12_WEALTH_TOTAL, ANSWER_A);
      suitability.put(S_Q13_HORIZON, ANSWER_D); // < 1 año
      suitability.put(S_Q14_LIQUIDITY, ANSWER_A);
      suitability.put(S_Q15_PURPOSE, ANSWER_E);
      suitability.put(S_Q16_LOSS_1MONTH, ANSWER_A);
      suitability.put(S_Q16_LOSS_1YEAR, ANSWER_A);
      suitability.put(S_Q16_LOSS_3YEARS, ANSWER_A);

      // Conveniencia: Sin estudios ni experiencia
      Map<Integer, String> convenience = new HashMap<>();
      convenience.put(C_Q1_EDUCATION, ANSWER_D); // Sin estudios
      convenience.put(C_Q2_PROFESSION, ANSWER_D);
      convenience.put(C_Q3_KNOWLEDGE, ANSWER_A); // Incorrecta

      SuitabilityResult result = calculator.calculateWithConvenience(suitability, convenience);

      // Verificamos que el perfil calculado es Conservador (Score 1)
      assertThat(result.profile()).isEqualTo(SuitabilityProfile.CONSERVATIVE);
      assertThat(result.alerts()).isEmpty(); // No debe haber alertas
    }

    @Test
    @DisplayName("Profile: Moderate")
    void testModerateProfile() {
      // 1. Idoneidad: Mantenemos perfil medio
      Map<Integer, String> suitability = createBaseSuitability();
      // Aseguramos Q16 en B (valor 2)
      suitability.put(S_Q16_LOSS_1MONTH, ANSWER_B);
      suitability.put(S_Q16_LOSS_1YEAR, ANSWER_B);
      suitability.put(S_Q16_LOSS_3YEARS, ANSWER_B);

      // 2. Conveniencia: Subimos puntos con experiencia
      Map<Integer, String> convenience = createBaseConvenience();

      // AÑADIMOS EXPERIENCIA:
      // Familia B (Renta fija simple) tiene valor 3 en Q10_FAMILY_VALUES[1].
      // C_Q10_BASE es 11, así que Familia B es la pregunta 12 (11 + 1).
      convenience.put(C_Q10_BASE + 1, TWO_OR_MORE);

      SuitabilityResult result = calculator.calculateWithConvenience(suitability, convenience);

      assertThat(result.profile()).isEqualTo(SuitabilityProfile.MODERATE);
      assertThat(result.alerts()).isEmpty();
    }

    @Test
    @DisplayName("Profile: Aggressive (High risk, Max Knowledge & Experience)")
    void testAggressiveProfile() {
      // Given
      Map<Integer, String> suitability = new HashMap<>();
      suitability.put(S_Q11_COMMITMENTS, ANSWER_E);
      suitability.put(S_Q12_WEALTH_BANK, ANSWER_D);
      suitability.put(S_Q12_WEALTH_TOTAL, ANSWER_D);
      suitability.put(S_Q13_HORIZON, ANSWER_A); // > 5 años
      suitability.put(S_Q14_LIQUIDITY, ANSWER_E);
      suitability.put(S_Q15_PURPOSE, ANSWER_A); // Maximizar rentabilidad
      suitability.put(S_Q16_LOSS_1MONTH, ANSWER_D); // > 20%
      suitability.put(S_Q16_LOSS_1YEAR, ANSWER_D);
      suitability.put(S_Q16_LOSS_3YEARS, ANSWER_D);

      // Conveniencia: Máxima puntuación
      Map<Integer, String> convenience = new HashMap<>();
      convenience.put(C_Q1_EDUCATION, ANSWER_A); // Máximo
      convenience.put(C_Q2_PROFESSION, ANSWER_A); // Máximo
      convenience.put(C_Q3_KNOWLEDGE, ANSWER_D); // Correcta

      // Experiencia en familias complejas (Familia C tiene valor alto)
      convenience.put(C_Q10_BASE + 2, TWO_OR_MORE);

      SuitabilityResult result = calculator.calculateWithConvenience(suitability, convenience);

      assertThat(result.profile()).isEqualTo(SuitabilityProfile.AGGRESSIVE);
      assertThat(result.alerts()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Block III (Knowledge) Tests")
  class BlockCalculationTests {

    @Test
    @DisplayName("Block III: Q3 correct answer boosts score")
    void testQ3_CorrectAnswer() {
      Map<Integer, String> suitability = createBaseSuitability();

      // Caso Incorrecto
      Map<Integer, String> convIncorrect = createBaseConvenience();
      convIncorrect.put(C_Q3_KNOWLEDGE, ANSWER_A); // Incorrecta (0 puntos)

      SuitabilityResult resIncorrect = calculator.calculateWithConvenience(suitability,
          convIncorrect);

      // Caso Correcto
      Map<Integer, String> convCorrect = createBaseConvenience();
      convCorrect.put(C_Q3_KNOWLEDGE, ANSWER_D); // Correcta (5 puntos)

      SuitabilityResult resCorrect = calculator.calculateWithConvenience(suitability, convCorrect);

      // Verificamos que acertar la pregunta mejora el perfil (o lo mantiene alto)
      assertThat(resCorrect.profile().getScore())
          .isGreaterThanOrEqualTo(resIncorrect.profile().getScore());
    }

    @Test
    @DisplayName("Block III: Q10 selects max value from families")
    void testQ10_ExperienceScoring() {
      Map<Integer, String> suitability = createBaseSuitability();
      Map<Integer, String> convenience = createBaseConvenience();

      // Añadir experiencia en Familia C (índice 2, valor 5) y Familia A (índice 0, valor 1)
      convenience.put(C_Q10_BASE, TWO_OR_MORE);     // Familia A -> Valor 1
      convenience.put(C_Q10_BASE + 2, TWO_OR_MORE); // Familia C -> Valor 5

      SuitabilityResult result = calculator.calculateWithConvenience(suitability, convenience);

      // Con estudios medios y Q3 correcta + exp 5, el Bloque III debería ser alto
      assertThat(result.profile().getScore()).isGreaterThanOrEqualTo(2);
    }
  }
}