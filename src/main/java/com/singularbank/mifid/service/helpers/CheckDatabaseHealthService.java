package com.singularbank.mifid.service.helpers;

import com.singularbank.mifid.entity.HealthResult;

public interface CheckDatabaseHealthService {
  HealthResult check();
}