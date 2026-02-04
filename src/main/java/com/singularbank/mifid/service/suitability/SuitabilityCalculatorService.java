package com.singularbank.mifid.service.suitability;

import com.singularbank.mifid.entity.SuitabilityProfile;
import com.singularbank.mifid.entity.SuitabilityResult;
import com.singularbank.mifid.repository.CombinacionRespuestaRepository;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuitabilityCalculatorService {

  private final CombinacionRespuestaRepository combinacionRepository;

  private static final class SuitabilityOrder {

    // Bloque II - Situación Financiera
    static final int Q11_COMMITMENTS = 1;
    static final int Q12_WEALTH_BANK = 3;
    static final int Q12_WEALTH_TOTAL = 4;

    // Bloque I - Objetivos de Inversión
    static final int Q13_HORIZON = 5;
    static final int Q14_LIQUIDITY = 6;
    static final int Q15_PURPOSE = 7;

    // Matriz Pérdida (Q16)
    static final int Q16_LOSS_1MONTH = 9;
    static final int Q16_LOSS_1YEAR = 10;
    static final int Q16_LOSS_3YEARS = 11;

    private SuitabilityOrder() {
    }
  }

  private static final class ConvenienceOrder {

    // Bloque III - Conocimientos y Experiencia (Vienen del test de Conveniencia)
    static final int Q1_EDUCATION = 1;
    static final int Q2_PROFESSION = 2;
    static final int Q3_KNOWLEDGE = 3;

    // Matriz Experiencia (Q10) - La familia A empieza en orden 11
    static final int Q10_BASE = 11;

    private ConvenienceOrder() {
    }
  }

  private record BlockScores(int blockI, int blockII, int blockIII) {

  }

  // --- Tablas de Puntuación (Lookups) ---
  private static final byte[] Q13_HORIZON_LOOKUP = new byte[5];
  private static final byte[] Q14_LIQUIDITY_LOOKUP = new byte[6];
  private static final byte[] Q15_PURPOSE_LOOKUP = new byte[6];
  private static final byte[] Q16_LOSS_LOOKUP = new byte[6];
  private static final byte[] Q1_EDUCATION_LOOKUP = new byte[5];
  private static final byte[] Q2_PROFESSION_LOOKUP = new byte[5];
  private static final byte[] Q11_COMMITMENTS_LOOKUP = new byte[6];

  static {
    // Inicialización de lookups según Excel IDONEIDAD
    Q13_HORIZON_LOOKUP['A'
        - 'A'] = 1; // Un mes (verificar vs Excel si A es 5 o 1) -> Excel fila 12: A=5, B=3, C=2, D=1.
    // CORRECCION SEGUN EXCEL (Fila 134):
    // A (No preveo... 5 años) -> 5 puntos
    // B (3-5 años) -> 3 puntos
    // C (1-3 años) -> 2 puntos
    // D (<1 año) -> 1 punto
    // Nota: En el código anterior estaba: A=1, B=3, C=4, D=5. ESTO PARECE INVERTIDO vs Excel.
    // Voy a ponerlo TAL CUAL el Excel Sheet "IDONEIDAD", Pregunta 13:
    // A -> 5
    // B -> 3
    // C -> 2
    // D -> 1
    // (Ajusta esto si en tu BBDD las letras están mapeadas diferente, pero sigo el Excel)
    Q13_HORIZON_LOOKUP['A' - 'A'] = 5;
    Q13_HORIZON_LOOKUP['B' - 'A'] = 3;
    Q13_HORIZON_LOOKUP['C' - 'A'] = 2;
    Q13_HORIZON_LOOKUP['D' - 'A'] = 1;

    Q14_LIQUIDITY_LOOKUP['A' - 'A'] = 1;
    Q14_LIQUIDITY_LOOKUP['B' - 'A'] = 2;
    Q14_LIQUIDITY_LOOKUP['C' - 'A'] = 3;
    Q14_LIQUIDITY_LOOKUP['D' - 'A'] = 4;
    Q14_LIQUIDITY_LOOKUP['E' - 'A'] = 5;

    Q15_PURPOSE_LOOKUP['A' - 'A'] = 5;
    Q15_PURPOSE_LOOKUP['B' - 'A'] = 4;
    Q15_PURPOSE_LOOKUP['C' - 'A'] = 3;
    Q15_PURPOSE_LOOKUP['D' - 'A'] = 2;
    Q15_PURPOSE_LOOKUP['E' - 'A'] = 1;

    Q16_LOSS_LOOKUP['A' - 'A'] = 1;
    Q16_LOSS_LOOKUP['B' - 'A'] = 2;
    Q16_LOSS_LOOKUP['C' - 'A'] = 3;
    Q16_LOSS_LOOKUP['D' - 'A'] = 4;
    Q16_LOSS_LOOKUP['E' - 'A'] = 5;

    Q1_EDUCATION_LOOKUP['A' - 'A'] = 5;
    Q1_EDUCATION_LOOKUP['B' - 'A'] = 4;
    Q1_EDUCATION_LOOKUP['C' - 'A'] = 2;
    Q1_EDUCATION_LOOKUP['D' - 'A'] = 1;

    Q2_PROFESSION_LOOKUP['A' - 'A'] = 5;
    Q2_PROFESSION_LOOKUP['B' - 'A'] = 4;
    Q2_PROFESSION_LOOKUP['C' - 'A'] = 3;
    Q2_PROFESSION_LOOKUP['D' - 'A'] = 1;

    Q11_COMMITMENTS_LOOKUP['A' - 'A'] = 1;
    Q11_COMMITMENTS_LOOKUP['B' - 'A'] = 2;
    Q11_COMMITMENTS_LOOKUP['C' - 'A'] = 3;
    Q11_COMMITMENTS_LOOKUP['D' - 'A'] = 4;
    Q11_COMMITMENTS_LOOKUP['E' - 'A'] = 5;
  }

  // Valores de la Pregunta 10 (Experiencia) para Familias A a K
  private static final byte[] Q10_FAMILY_VALUES = {
      1, 3, 5, 4, 2, 5, 4, 4, 5, 5, 5
  };

  private static final double Q1_WEIGHT = 0.15;
  private static final double Q2_WEIGHT = 0.15;
  private static final double Q3_WEIGHT = 0.30;
  private static final double Q10_WEIGHT = 0.40;

  // Matrices de decisión (Mapeadas del Excel)
  private static final byte[][] MATRIX_Q15_Q16 = {
      {1, 1, 2, 2, 2}, {1, 2, 2, 3, 3}, {2, 2, 3, 3, 4},
      {2, 3, 3, 4, 4}, {2, 3, 4, 4, 5}
  };

  private static final byte[][] MATRIX_BLOCK_I_COMBINATION = {
      {1, 1, 1, 2, 2}, {1, 1, 2, 2, 3}, {1, 2, 2, 3, 4},
      {2, 2, 3, 4, 4}, {2, 3, 4, 4, 5}
  };

  private static final byte[][] MATRIX_BLOCKS_I_II = {
      {1, 1, 1, 2, 2}, {1, 1, 2, 2, 3}, {1, 2, 2, 3, 4},
      {2, 2, 3, 4, 4}, {2, 3, 4, 4, 5}
  };

  private static final byte[][] MATRIX_FINAL = {
      {1, 1, 1, 1, 1}, {1, 1, 2, 2, 3}, {1, 2, 2, 3, 4},
      {1, 2, 3, 4, 5}, {1, 2, 3, 4, 5}
  };

  private static final String CORRECT_ANSWER = "D";

  /**
   * Calcula la Idoneidad utilizando las respuestas propias y las de Conveniencia (Bloque III). No
   * genera alertas, ya que el modelo de Idoneidad del Excel no define alertas de incoherencia.
   */
  public SuitabilityResult calculateWithConvenience(
      Map<Integer, String> suitabilityResponsesByOrder,
      Map<Integer, String> convenienceResponsesByOrder) {

    try {
      var scores = calculateAllBlocks(suitabilityResponsesByOrder, convenienceResponsesByOrder);

      // Combinación Bloques I y II
      // El índice de la matriz es (valor - 1)
      int combinedIAndII = MATRIX_BLOCKS_I_II[scores.blockI - 1][scores.blockII - 1];

      // Valor final: Combinación (I+II) con Bloque III
      int finalScore = MATRIX_FINAL[combinedIAndII - 1][scores.blockIII - 1];

      var profile = SuitabilityProfile.fromScore(finalScore);

      // Calcular combinationId basado en el perfil para persistencia
      String profileName = profile.getDisplayName();
      Integer combinationId = combinacionRepository.findByProfileName(profileName);
      log.info("Suitability: Profile '{}' mapped to combination ID {}", profileName, combinationId);

      // Idoneidad no devuelve alertas según la definición funcional actual
      return new SuitabilityResult(profile, Collections.emptyList(), combinationId);

    } catch (Exception e) {
      log.error("Error calculating suitability", e);
      throw new IllegalStateException("Error in suitability calculation", e);
    }
  }

  private BlockScores calculateAllBlocks(Map<Integer, String> sResponses,
      Map<Integer, String> cResponses) {
    int blockIII = calculateBlockIII(cResponses);
    int blockI = calculateBlockI(sResponses);
    int blockII = calculateBlockII(sResponses);
    return new BlockScores(blockI, blockII, blockIII);
  }

  /**
   * BLOQUE III: Conocimientos y Experiencia. Se nutre exclusivamente de respuestas del test de
   * CONVENIENCIA.
   */
  private int calculateBlockIII(Map<Integer, String> cResponses) {
    // Q1 Estudios
    double q1Score =
        getValueFast(cResponses.get(ConvenienceOrder.Q1_EDUCATION), Q1_EDUCATION_LOOKUP, 2)
            * Q1_WEIGHT;

    // Q2 Profesión
    double q2Score =
        getValueFast(cResponses.get(ConvenienceOrder.Q2_PROFESSION), Q2_PROFESSION_LOOKUP, 1)
            * Q2_WEIGHT;

    // Q3 Conocimientos Financieros
    double q3Score =
        (CORRECT_ANSWER.equals(cResponses.get(ConvenienceOrder.Q3_KNOWLEDGE)) ? 5 : 0) * Q3_WEIGHT;

    // Q10 Experiencia (Matriz)
    double q10Score = calculateQ10ExperienceFast(cResponses) * Q10_WEIGHT;

    // Suma ponderada con mínimo de 1 y redondeo
    return Math.clamp((int) Math.round(q1Score + q2Score + q3Score + q10Score), 1, 5);
  }

  private int calculateQ10ExperienceFast(Map<Integer, String> cResponses) {
    int maxValue = 0;

    for (int i = 0; i < Q10_FAMILY_VALUES.length; i++) {
      // ConvenienceOrder.Q10_BASE = 11. Iteramos por las familias A(11) a K(21)
      String response = cResponses.get(ConvenienceOrder.Q10_BASE + i);

      // "A" corresponde a "Dos o más" operaciones
      if ("A".equals(response) && Q10_FAMILY_VALUES[i] > maxValue) {
        maxValue = Q10_FAMILY_VALUES[i];
        if (maxValue == 5) {
          break; // Máximo alcanzado
        }
      }
    }
    return maxValue;
  }

  /**
   * BLOQUE I: Objetivos de Inversión. Utiliza las respuestas del test de IDONEIDAD.
   */
  private int calculateBlockI(Map<Integer, String> sResponses) {
    // Combinación Pregunta 16 (Pérdida Máxima) -> Valor único
    int q16Combined = calculateQ16CombinedFast(sResponses);

    // Pregunta 15 (Finalidad)
    int q15Value = getValueFast(sResponses.get(SuitabilityOrder.Q15_PURPOSE), Q15_PURPOSE_LOOKUP,
        3);

    // Matriz Q15 y Q16
    int q15And16Combined = MATRIX_Q15_Q16[q15Value - 1][q16Combined - 1];

    // Pregunta 13 (Horizonte)
    int q13Value = getValueFast(sResponses.get(SuitabilityOrder.Q13_HORIZON), Q13_HORIZON_LOOKUP,
        3);

    // Pregunta 14 (Liquidez)
    int q14Value = getValueFast(sResponses.get(SuitabilityOrder.Q14_LIQUIDITY),
        Q14_LIQUIDITY_LOOKUP, 3);

    // Media de Q13 y Q14 redondeada
    int q13And14Average = Math.clamp((q13Value + q14Value + 1) >> 1, 1, 5);

    // Matriz final Bloque I
    return MATRIX_BLOCK_I_COMBINATION[q15And16Combined - 1][q13And14Average - 1];
  }

  private int calculateQ16CombinedFast(Map<Integer, String> sResponses) {
    int val1Month = getValueFast(sResponses.get(SuitabilityOrder.Q16_LOSS_1MONTH), Q16_LOSS_LOOKUP,
        1);
    int val1Year = getValueFast(sResponses.get(SuitabilityOrder.Q16_LOSS_1YEAR), Q16_LOSS_LOOKUP,
        3);
    int val3Years = getValueFast(sResponses.get(SuitabilityOrder.Q16_LOSS_3YEARS), Q16_LOSS_LOOKUP,
        4);

    boolean highLossTolerance = val1Year >= 4 && val3Years >= 4;

    // Lógica tabla Q16
    return switch (val1Month) {
      case 1 -> 1;
      case 2 -> 2;
      case 3 -> highLossTolerance ? 4 : 3;
      case 4 -> (val1Year == 5 && val3Years == 5) ? 5 : 4;
      default -> highLossTolerance ? 5 : 4;
    };
  }

  /**
   * BLOQUE II: Situación Financiera. Utiliza las respuestas del test de IDONEIDAD.
   */
  private int calculateBlockII(Map<Integer, String> sResponses) {
    // Pregunta 11 (Compromisos financieros)
    int q11Value = getValueFast(sResponses.get(SuitabilityOrder.Q11_COMMITMENTS),
        Q11_COMMITMENTS_LOOKUP, 3);

    // Pregunta 12 (Patrimonio) - Media de sus dos sub-preguntas
    int q12Average = calculateQ12WealthFast(sResponses);

    // Condicionante P11: Si P11 < 3, se resta 1 a P12
    int q12Adjusted = (q11Value < 3 && q12Average > 1) ? q12Average - 1 : q12Average;

    // Resultado final Bloque II: Media de P11 y P12(ajustada)
    return Math.clamp((int) Math.round((q11Value + q12Adjusted) / 2.0), 1, 5);
  }

  private int calculateQ12WealthFast(Map<Integer, String> sResponses) {
    int valBank = getWealthBankValue(sResponses.get(SuitabilityOrder.Q12_WEALTH_BANK));
    int valTotal = getWealthTotalValue(sResponses.get(SuitabilityOrder.Q12_WEALTH_TOTAL));
    return Math.clamp((int) Math.round((valBank + valTotal) / 2.0), 1, 5);
  }

  private int getWealthBankValue(String response) {
    if (response == null || response.isEmpty()) {
      return 3;
    }
    return switch (response.charAt(0)) {
      case 'A' -> 2;
      case 'B' -> 3;
      case 'C' -> 4;
      case 'D' -> 5;
      default -> 3;
    };
  }

  private int getWealthTotalValue(String response) {
    if (response == null || response.isEmpty()) {
      return 2;
    }
    return switch (response.charAt(0)) {
      case 'A' -> 1;
      case 'B' -> 2;
      case 'C' -> 3;
      case 'D' -> 4;
      default -> 2;
    };
  }

  private int getValueFast(String response, byte[] lookup, int defaultValue) {
    if (response == null || response.isEmpty()) {
      return defaultValue;
    }
    char c = response.charAt(0);
    int index = c - 'A';
    return (index >= 0 && index < lookup.length) ? lookup[index] : defaultValue;
  }
}