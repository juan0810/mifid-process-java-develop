package com.singularbank.mifid.service.pdf;

import com.singularbank.mifid.entity.FileDownload;

public interface GeneratorPdfService {

  FileDownload generate(String documentNumber);
  FileDownload generateByID(Integer testId);
}