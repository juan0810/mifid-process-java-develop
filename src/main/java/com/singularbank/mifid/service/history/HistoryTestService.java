package com.singularbank.mifid.service.history;

import com.singularbank.mifid.entity.HistoryTestFilter;
import com.singularbank.mifid.entity.HistoryTestPage;

public interface HistoryTestService {

  HistoryTestPage getTestHistory(String documentNumber, HistoryTestFilter filter);
}
