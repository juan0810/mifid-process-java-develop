package com.singularbank.mifid.config.tracing;

public interface CorrelationProvider {
    String getCorrelationId();
}