package com.singularbank.mifid.service.convenience;

import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.ItemRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConvenienceQuestionIdLoader {

  private final ItemRepository itemRepository;

  private final Map<Short, Map<Short, Integer>> versionOrderToIdCache = new HashMap<>();

  public Integer getIdByOrder(short versionId, short order) {
    Map<Short, Integer> orderToIdMap = versionOrderToIdCache.computeIfAbsent(
        versionId,
        this::loadQuestionIdsForVersion
    );
    
    Integer id = orderToIdMap.get(order);
    if (id == null) {
      log.warn("No question ID found for order {} in convenience test for version {}", order, versionId);
    }
    return id;
  }

  private Map<Short, Integer> loadQuestionIdsForVersion(Short versionId) {
    try {
      log.info("Loading convenience question IDs for version {}...", versionId);

      List<Question> questions = itemRepository.findByVersionIdAndTestType(versionId, TypeTest.CONVENIENCE);
      
      if (questions.isEmpty()) {
        log.warn("No convenience questions found for version {}. Using empty map.", versionId);
        return Map.of();
      }
      
      Map<Short, Integer> orderToIdMap = questions.stream()
          .collect(Collectors.toMap(
              Question::getOrder,
              Question::getId,
              (existing, replacement) -> {
                log.warn("Duplicate order {} found for version {}. Keeping first occurrence.", 
                    replacement, versionId);
                return existing;
              }
          ));
      
      log.info("Loaded {} convenience question IDs for version {}", orderToIdMap.size(), versionId);
      return orderToIdMap;
      
    } catch (Exception e) {
      log.error("Failed to load convenience question IDs for version {}. Using empty map.", versionId, e);
      return Map.of();
    }
  }

  public void clearCache(Short versionId) {
    versionOrderToIdCache.remove(versionId);
    log.info("Cleared cache for version {}", versionId);
  }

  public void clearAllCache() {
    versionOrderToIdCache.clear();
    log.info("Cleared all version caches");
  }
}
