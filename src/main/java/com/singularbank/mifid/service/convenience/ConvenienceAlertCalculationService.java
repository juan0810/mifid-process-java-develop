package com.singularbank.mifid.service.convenience;

import com.singularbank.mifid.entity.ConvenienceResult;
import com.singularbank.mifid.entity.StoreTestAnswers;

public interface ConvenienceAlertCalculationService {

  ConvenienceResult calculateAlerts(
      String documentNumber,
      StoreTestAnswers storeTestAnswers);
}