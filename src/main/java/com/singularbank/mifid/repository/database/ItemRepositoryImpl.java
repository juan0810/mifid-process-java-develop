package com.singularbank.mifid.repository.database;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.ItemRepository;
import com.singularbank.mifid.repository.database.jpa.JpaItemRepository;
import com.singularbank.mifid.repository.database.mapper.ItemEntityMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

  private final JpaItemRepository jpaItemRepository;
  private final ItemEntityMapper mapper;

  @Override
  public Short getLastVersion(TypeTest typeTest, String application) {
    return jpaItemRepository.findLastVersion(typeTest.getCode(), application);
  }

  @Override
  public List<Question> findQuestionsWithAnswersByTestType(TypeTest typeTest, String application,
      Short version) {
    return jpaItemRepository.findQuestionsWithAnswersByTestType(typeTest.getCode(), application,
            version)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<Question> findAllQuestionsByTestType(TypeTest typeTest, String application,
      Short version) {
    return jpaItemRepository.findAllQuestionsByTestType(typeTest.getCode(), application, version)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<Answer> findAnswersByIdsAndTestType(List<Integer> ids, TypeTest typeTest) {
    return jpaItemRepository.findAnswersByIdsAndTestType(ids, typeTest.getCode())
        .stream()
        .map(mapper::toAnswer)
        .toList();
  }

  @Override
  public List<Answer> findAnswersByIds(List<Integer> ids) {
    return jpaItemRepository.findAnswersByIds(ids)
        .stream()
        .map(mapper::toAnswer)
        .toList();
  }

  @Override
  public List<Question> findByVersionIdAndTestType(Short versionId, TypeTest typeTest) {
    return jpaItemRepository.findQuestionsByVersionIdAndTestType(versionId, typeTest.getCode())
        .stream()
        .map(mapper::toDomain)
        .toList();
  }
}