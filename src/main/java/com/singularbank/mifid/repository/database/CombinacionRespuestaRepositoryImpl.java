package com.singularbank.mifid.repository.database;

import com.singularbank.mifid.repository.CombinacionRespuestaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CombinacionRespuestaRepositoryImpl implements CombinacionRespuestaRepository {
    
    @Override
    public Integer findByFamilyCode(String familia, boolean conveniente) {
        // CRID 20-30: Convenientes (A=20, B=21, ... K=30)
        // CRID 31-41: No convenientes (A=31, B=32, ... K=41)
        int baseId = conveniente ? 20 : 31;
        int offset = familia.charAt(0) - 'A';  // A=0, B=1, ... K=10
        return baseId + offset;
    }
    
    @Override
    public Integer findByProfileName(String profileName) {
        return switch (profileName) {
            case "Conservador" -> 10;
            case "Moderado" -> 11;
            case "Equilibrado" -> 12;
            case "Decidido" -> 13;
            case "Agresivo" -> 14;
            default -> {
                log.warn("Unknown profile name: {}", profileName);
                yield null;
            }
        };
    }
}
