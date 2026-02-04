package com.singularbank.mifid.service.status;

import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.exception.BadRequestException;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StateTransitionValidator {

  private static final Set<StateTest> NON_TRANSITIONABLE_TARGETS = Set.of(StateTest.EXPIRED);

  private static final Map<StateTest, Set<StateTest>> ALLOWED_TRANSITIONS = Map.of(
      StateTest.DRAFT,
      Set.of(StateTest.DRAFT, StateTest.PENDING, StateTest.SIGNED, StateTest.CANCELLED),
      StateTest.PENDING, Set.of(StateTest.SIGNED, StateTest.CANCELLED),
      StateTest.SIGNED, Set.of(),
      StateTest.CANCELLED, Set.of(),
      StateTest.EXPIRED, Set.of()
  );

  public void validate(StateTest current, StateTest target) {
    if (NON_TRANSITIONABLE_TARGETS.contains(target)) {
      throw new BadRequestException(
          "Cannot manually transition to %s. This state is set automatically.".formatted(target));
    }

    var allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());

    if (!allowed.contains(target)) {
      throw new BadRequestException(
          allowed.isEmpty()
              ? "Cannot change status from %s. It's a final state.".formatted(current)
              : "Invalid transition from %s to %s. Allowed: %s".formatted(current, target,
                  allowed));
    }
  }
}