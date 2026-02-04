package com.singularbank.mifid.service.status;

import com.singularbank.mifid.entity.StateTest;
import com.singularbank.mifid.entity.StatusTestResult;

public interface StatusTestService {

  StatusTestResult updateStatus(Integer testId, StateTest newStatus);
}