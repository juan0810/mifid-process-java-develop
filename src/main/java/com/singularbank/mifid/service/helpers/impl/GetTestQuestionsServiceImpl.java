package com.singularbank.mifid.service.helpers.impl;

import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.QuestionsTest;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.entity.Version;
import com.singularbank.mifid.exception.ResourceNotFoundException;
import com.singularbank.mifid.repository.ItemRepository;
import com.singularbank.mifid.repository.VersionRepository;
import com.singularbank.mifid.service.helpers.GetTestQuestionsService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetTestQuestionsServiceImpl implements GetTestQuestionsService {

  private final ItemRepository itemRepository;
  private final VersionRepository versionRepository;

  @Override
  @Transactional(readOnly = true)
  public QuestionsTest getTest(
      String application,
      TypeTest typeTest,
      Short version) {

    Objects.requireNonNull(application, "application is required");
    Objects.requireNonNull(typeTest, "testType is required");

    Short effectiveVersion = resolveVersion(typeTest, application, version);

    log.info("Fetching {} test for application: '{}', version: {}",
        typeTest, application, effectiveVersion);

    List<Question> questions = itemRepository
        .findQuestionsWithAnswersByTestType(typeTest, application, effectiveVersion);

    if (CollectionUtils.isEmpty(questions)) {
      throw new ResourceNotFoundException(
          "No %s test found for application='%s' and version=%d"
              .formatted(typeTest.name().toLowerCase(), application, effectiveVersion)
      );
    }

    Version versionEntity = versionRepository
        .findVersionesActivasById(effectiveVersion, application);

    log.debug("Found {} questions for {} test (application='{}', version={})",
        questions.size(), typeTest, application, effectiveVersion);

    return QuestionsTest.builder()
        .version(versionEntity)
        .questions(questions)
        .build();
  }

  private Short resolveVersion(TypeTest typeTest, String application, Short version) {
    if (version != null) {
      log.debug("Using provided version: {}", version);
      return version;
    }

    log.debug("No version provided, fetching latest for {} test with application='{}'",
        typeTest, application);

    Short latestVersion = itemRepository.getLastVersion(typeTest, application);

    if (latestVersion == null) {
      throw new ResourceNotFoundException(
          "No versions found for %s test with application='%s'"
              .formatted(typeTest.name().toLowerCase(), application)
      );
    }

    log.info("Using latest version {} for {} test with application='{}'",
        latestVersion, typeTest, application);
    return latestVersion;
  }
}