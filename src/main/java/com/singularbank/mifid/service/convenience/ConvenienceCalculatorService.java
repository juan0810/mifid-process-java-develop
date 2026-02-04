package com.singularbank.mifid.service.convenience;

import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.repository.CombinacionMatchingRepository;
import com.singularbank.mifid.repository.CombinacionRespuestaRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvenienceCalculatorService {

  private final CombinacionRespuestaRepository combinacionRepository;
  private final ConvenienceQuestionIdLoader questionIdLoader;
  private final CombinacionMatchingRepository combinacionMatchingRepository;

  private static final class QuestionId {

    static final int Q1_EDUCATION = 1;
    static final int Q2_PROFESSION = 2;
    static final int Q3_GENERAL_KNOWLEDGE = 3;
    static final int Q4_FAMILY_E_KNOWLEDGE = 4;
    static final int Q5_FAMILY_F_KNOWLEDGE = 5;
    static final int Q6_FAMILY_G_KNOWLEDGE = 6;
    static final int Q7_FAMILY_H_KNOWLEDGE = 7;
    static final int Q8_FAMILY_I_KNOWLEDGE = 8;
    static final int Q9_FAMILY_JK_KNOWLEDGE = 9;

    // P10 se carga dinámicamente
    private QuestionId() {
    }
  }

  private static final class Answer {

    static final String A = "A"; // Dos o más veces / Estudios Técnicos / Prof Financiero
    static final String B = "B"; // Una o ninguna / Otros Univ / Prof Relacionado
    static final String C = "C"; // Bachillerato / Prof Indirecto
    static final String D = "D"; // Sin estudios / Sin relación
    static final String CORRECT_GENERAL = "D";

    private Answer() {
    }
  }

  sealed interface EvaluationResult permits Convenient, NotConvenient, Alert {

  }

  record Convenient() implements EvaluationResult {

  }

  record NotConvenient(String reason) implements EvaluationResult {

  }

  record Alert(boolean convenient, AlertType type, String message) implements EvaluationResult {

  }

  enum AlertType {INCOHERENCE, ATYPICAL}

  private static final String MSG_NO_FAMILIES = "No families were evaluated";
  private static final String MSG_INSUFFICIENT_GENERAL = "Insufficient general financial knowledge";
  private static final String MSG_INSUFFICIENT_SPECIFIC = "Insufficient specific product knowledge";
  private static final String MSG_INCOHERENCE_PROF = "Has professional experience but fails knowledge test";
  private static final String MSG_ATYPICAL_NO_EDU_WITH_EXP = "No higher education but has product experience";
  private static final String MSG_NOT_MEET_CRITERIA = "Does not meet convenience criteria";
  private static final String MSG_INVALID_EDUCATION = "Invalid education level";

  private static final Set<String> COMPLEX_FAMILIES = Set.of("E", "F", "G", "H", "I", "J", "K");

  private static final Map<String, Integer> SPECIFIC_KNOWLEDGE_QUESTION = Map.of(
      "E", QuestionId.Q4_FAMILY_E_KNOWLEDGE,
      "F", QuestionId.Q5_FAMILY_F_KNOWLEDGE,
      "G", QuestionId.Q6_FAMILY_G_KNOWLEDGE,
      "H", QuestionId.Q7_FAMILY_H_KNOWLEDGE,
      "I", QuestionId.Q8_FAMILY_I_KNOWLEDGE,
      "J", QuestionId.Q9_FAMILY_JK_KNOWLEDGE,
      "K", QuestionId.Q9_FAMILY_JK_KNOWLEDGE
  );

  private static final Map<Integer, String> CORRECT_ANSWERS = Map.of(
      QuestionId.Q3_GENERAL_KNOWLEDGE, Answer.D,
      QuestionId.Q4_FAMILY_E_KNOWLEDGE, Answer.C,
      QuestionId.Q5_FAMILY_F_KNOWLEDGE, Answer.B,
      QuestionId.Q6_FAMILY_G_KNOWLEDGE, Answer.B,
      QuestionId.Q7_FAMILY_H_KNOWLEDGE, Answer.D,
      QuestionId.Q8_FAMILY_I_KNOWLEDGE, Answer.B,
      QuestionId.Q9_FAMILY_JK_KNOWLEDGE, Answer.C
  );

  public ConvenienceResult calculateConvenienceDetailed(Map<Integer, String> responses,
      Short versionId) {
    Map<Integer, String> q10ToFamily = buildQ10ToFamilyMap(versionId);
    var families = extractEvaluatedFamilies(responses, q10ToFamily);

    if (families.isEmpty()) {
      log.warn("No families to evaluate");
      return ConvenienceResult.builder().alerts(List.of(MSG_NO_FAMILIES)).build();
    }

    var evaluations = evaluateAllFamilies(families, responses, q10ToFamily);
    return buildResult(evaluations);
  }

  private Map<Integer, String> buildQ10ToFamilyMap(Short versionId) {
    return Map.ofEntries(
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 11), "A"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 12), "B"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 13), "C"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 14), "D"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 15), "E"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 16), "F"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 17), "G"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 18), "H"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 19), "I"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 20), "J"),
        Map.entry(questionIdLoader.getIdByOrder(versionId, (short) 21), "K")
    );
  }

  private Set<String> extractEvaluatedFamilies(Map<Integer, String> responses,
      Map<Integer, String> q10ToFamily) {
    return q10ToFamily.entrySet().stream()
        .filter(entry -> responses.containsKey(entry.getKey()))
        .map(Map.Entry::getValue)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private Map<String, EvaluationResult> evaluateAllFamilies(
      Set<String> families, Map<Integer, String> responses, Map<Integer, String> q10ToFamily) {

    Map<String, Integer> familyToQ10 = q10ToFamily.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    var context = new EvaluationContext(
        responses.get(QuestionId.Q1_EDUCATION),
        responses.get(QuestionId.Q2_PROFESSION),
        responses.get(QuestionId.Q3_GENERAL_KNOWLEDGE),
        responses
    );

    Map<String, EvaluationResult> results = new LinkedHashMap<>();
    for (String family : families) {
      results.put(family, evaluateFamily(family, context, familyToQ10));
    }
    return results;
  }

  private EvaluationResult evaluateFamily(String family, EvaluationContext ctx,
      Map<String, Integer> familyToQ10) {
    log.debug("Evaluating family: {}, Q3={}, Q2={}, Q1={}", family, ctx.q3, ctx.q2, ctx.q1);

    // 1. Validar Conocimientos Generales (Q3)
    var generalCheck = checkGeneralKnowledge(ctx.q3, ctx.q2);
    if (generalCheck != null) {
      return generalCheck;
    }

    // 2. Validar Conocimientos Específicos (solo si es compleja)
    boolean isComplex = COMPLEX_FAMILIES.contains(family);
    if (isComplex) {
      var specificCheck = checkSpecificKnowledge(family, ctx);
      if (specificCheck != null) {
        return specificCheck;
      }
    }

    // 3. Matriz de decisión (Estudios vs Experiencia vs Profesión)
    Integer experienceQuestionId = familyToQ10.get(family);
    String productExperience = ctx.responses.get(experienceQuestionId);

    return applyConvenienceMatrix(ctx.q1, ctx.q2, productExperience, isComplex);
  }

  private EvaluationResult checkGeneralKnowledge(String q3Answer, String profession) {
    if (Answer.CORRECT_GENERAL.equals(q3Answer)) {
      return null;
    }
    // Si falla conocimientos, comprobamos si hay incoherencia por ser profesional
    return hasProfessionalExperience(profession)
        ? new Alert(false, AlertType.INCOHERENCE, MSG_INCOHERENCE_PROF)
        : new NotConvenient(MSG_INSUFFICIENT_GENERAL);
  }

  private EvaluationResult checkSpecificKnowledge(String family, EvaluationContext ctx) {
    Integer questionId = SPECIFIC_KNOWLEDGE_QUESTION.get(family);
    if (questionId == null) {
      return null;
    }
    String clientAnswer = ctx.responses.get(questionId);
    String correctAnswer = CORRECT_ANSWERS.get(questionId);

    if (correctAnswer.equals(clientAnswer)) {
      return null;
    }
    // Si falla específicos, comprobamos si hay incoherencia
    return hasProfessionalExperience(ctx.q2)
        ? new Alert(false, AlertType.INCOHERENCE, MSG_INCOHERENCE_PROF)
        : new NotConvenient(MSG_INSUFFICIENT_SPECIFIC);
  }

  private EvaluationResult applyConvenienceMatrix(
      String education,
      String profession,
      String productExperience,
      boolean isComplex) {

    return switch (education) {
      case Answer.A -> matrixTechnicalUniversity();
      case Answer.B -> matrixOtherUniversity(profession, productExperience, isComplex);
      case Answer.C -> matrixBachelor(productExperience);
      case Answer.D -> matrixNoHigherEducation(profession, productExperience, isComplex);
      case null, default -> new NotConvenient(MSG_INVALID_EDUCATION);
    };
  }

  // Nivel A: Estudios Técnicos / Económicos
// Excel No Complejos Fila 7: A,D,D,B -> CONVENIENTE
// Excel Complejos Fila 16: A,D,D,B -> CONVENIENTE
// Conclusión: Siempre conveniente (si ha pasado los tests de conocimientos previos)
  private EvaluationResult matrixTechnicalUniversity() {
    return convenient();
  }

  // Nivel B: Otros Universitarios
  private EvaluationResult matrixOtherUniversity(String prof, String exp, boolean isComplex) {
    // Si tiene experiencia inversora (A), siempre es Conveniente (Filas 39 y 78)
    if (Answer.A.equals(exp)) {
      return convenient();
    }

    // Si NO tiene experiencia inversora (B):
    if (isComplex) {
      // En Complejos, sin experiencia es NO Conveniente, incluso siendo banquero (Fila 92)
      return notConvenient();
    } else {
      // En No Complejos, es Conveniente SOLO si tiene perfil profesional alto (A o B)
      // Fila 46 (Prof A) -> Conv. Fila 40 (Prof D) -> NC.
      return isHighLevelProfession(prof) ? convenient() : notConvenient();
    }
  }

  // Nivel C: Bachillerato / FP
  private EvaluationResult matrixBachelor(String exp) {
    // Excel No Complejos Fila 71: Exp A -> Conv. Fila 72: Exp B -> NC.
    // Excel Complejos Fila 142: Exp A -> Conv. Fila 156: Prof A + Exp B -> NC.
    // Conclusión: Solo la experiencia en producto determina la conveniencia.
    return Answer.A.equals(exp) ? convenient() : notConvenient();
  }

  // Nivel D: Sin estudios / Básicos
  private EvaluationResult matrixNoHigherEducation(String prof, String exp, boolean isComplex) {
    if (isComplex) {
      return notConvenient();
    }

    // Para no complejos, si tiene experiencia (A), genera ALERTA ATÍPICA (pero es conveniente)
    // Fila 103: Exp A -> Alerta (Conv). Fila 104: Exp B -> Alerta (NC).
    return switch (prof) {
      case Answer.A, Answer.B -> Answer.A.equals(exp)
          ? new Alert(true, AlertType.ATYPICAL, MSG_ATYPICAL_NO_EDU_WITH_EXP)
          : notConvenient();
      case Answer.C -> Answer.A.equals(exp)
          ? convenient()
          : notConvenient();
      case null, default -> Answer.A.equals(exp)
          // Si no tiene profesión ni estudios pero sí experiencia -> Alerta + Conv
          ? new Alert(true, AlertType.ATYPICAL, MSG_ATYPICAL_NO_EDU_WITH_EXP)
          : notConvenient();
    };
  }

  private boolean hasProfessionalExperience(String profession) {
    return Answer.A.equals(profession) ||
        Answer.B.equals(profession) ||
        Answer.C.equals(profession);
  }

  private boolean isHighLevelProfession(String profession) {
    return Answer.A.equals(profession) || Answer.B.equals(profession);
  }

  private EvaluationResult convenient() {
    return new Convenient();
  }

  private EvaluationResult notConvenient() {
    return new NotConvenient(MSG_NOT_MEET_CRITERIA);
  }

  private ConvenienceResult buildResult(Map<String, EvaluationResult> evaluations) {
    var convenientFamilies = new LinkedHashSet<String>();
    var notConvenientFamilies = new LinkedHashSet<String>();
    var familiesWithAlerts = new LinkedHashSet<String>();
    var alerts = new ArrayList<String>();

    evaluations.forEach((family, result) -> {
      switch (result) {
        case Convenient() -> convenientFamilies.add(family);
        case NotConvenient(String reason) -> notConvenientFamilies.add(family);
        case Alert(boolean isConvenient, AlertType type, String message) -> {
          (isConvenient ? convenientFamilies : notConvenientFamilies).add(family);
          familiesWithAlerts.add(family);
          alerts.add("Family %s: %s - %s".formatted(family, type, message));

          log.warn("Family {}: {} [ALERT: {}]", family,
              isConvenient ? "CONVENIENT" : "NOT CONVENIENT", type);
        }
      }
    });

    // Calcular combinationIds (lógica mantenida)
    List<Integer> combinationIds = new ArrayList<>();
    for (String family : convenientFamilies) {
      Integer crid = combinacionRepository.findByFamilyCode(family, true);
      if (crid != null) {
        combinationIds.add(crid);
      }
    }
    for (String family : notConvenientFamilies) {
      Integer crid = combinacionRepository.findByFamilyCode(family, false);
      if (crid != null) {
        combinationIds.add(crid);
      }
    }

    return ConvenienceResult.builder()
        .convenientFamilies(convenientFamilies)
        .notConvenientFamilies(notConvenientFamilies)
        .familiesWithAlerts(familiesWithAlerts)
        .alerts(alerts)
        .combinationIds(combinationIds)
        .build();
  }

  private record EvaluationContext(
      String q1,
      String q2,
      String q3,
      Map<Integer, String> responses
  ) {

  }
}