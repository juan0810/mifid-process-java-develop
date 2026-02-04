package com.singularbank.mifid.repository;

import com.singularbank.mifid.entity.Answer;
import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.TypeTest;
import java.util.List;

public interface ItemRepository {

  Short getLastVersion(TypeTest typeTest, String application);

  List<Question> findQuestionsWithAnswersByTestType(TypeTest typeTest, String application, Short version);

  List<Question> findAllQuestionsByTestType(TypeTest typeTest, String application, Short version);

  List<Answer> findAnswersByIdsAndTestType(List<Integer> ids, TypeTest typeTest);

  List<Answer> findAnswersByIds(List<Integer> ids);

  List<Question> findByVersionIdAndTestType(Short versionId, TypeTest typeTest);
}