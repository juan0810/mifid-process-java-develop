package com.singularbank.mifid.repository;

import java.util.Map;

public interface GeneratorPdfRepository {

  String generatePdfBase64(Map<String, Object> parameters);
}