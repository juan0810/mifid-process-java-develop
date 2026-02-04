package com.singularbank.mifid.config.tracing;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalCorrelationProvider implements CorrelationProvider {
    @Override
    public String getCorrelationId() {
        return CorrelationFilter.getCorrelationId();
    }
}