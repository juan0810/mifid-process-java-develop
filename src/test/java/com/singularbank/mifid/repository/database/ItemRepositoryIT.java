package com.singularbank.mifid.repository.database;

import static org.assertj.core.api.Assertions.assertThat;

import com.singularbank.mifid.entity.Question;
import com.singularbank.mifid.entity.TypeTest;
import com.singularbank.mifid.repository.ItemRepository;
import com.singularbank.mifid.repository.config.AbstractRepositoryTest;
import com.singularbank.mifid.repository.database.jpa.JpaItemRepository;
import com.singularbank.mifid.repository.database.mapper.ItemEntityMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ItemRepositoryIT extends AbstractRepositoryTest {

  @Autowired
  private JpaItemRepository jpaItemRepository;

  @Autowired
  private ItemEntityMapper mapper;

  private ItemRepository itemRepository;

  private static final String APPLICATION_ONBOARDING = "ONBOARDING";
  private static final Short VERSION_1 = 1;

  @BeforeEach
  void setUp() {
    itemRepository = new ItemRepositoryImpl(jpaItemRepository, mapper);
  }

  @Test
  void shouldGetLastVersionForConvenienceTest() {
    Short lastVersion = itemRepository.getLastVersion(
        TypeTest.CONVENIENCE,
        APPLICATION_ONBOARDING
    );
    assertThat(lastVersion).isNotNull().isGreaterThan((short) 0);
  }

  @Test
  void shouldFindQuestionsWithAnswersByTestType() {
    List<Question> questions = itemRepository
        .findQuestionsWithAnswersByTestType(
            TypeTest.CONVENIENCE,
            APPLICATION_ONBOARDING,
            VERSION_1
        );
    assertThat(questions).isNotNull().isNotEmpty();
  }
}
