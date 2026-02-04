# svc-mifid-process-java

Servicio Spring Boot para la gestión de tests MiFID (Markets in Financial Instruments Directive) incluyendo tests de idoneidad, conveniencia y sostenibilidad.

## 📋 Descripción

Este servicio gestiona los tests obligatorios MiFID II que deben realizar los clientes antes de la contratación de productos financieros:

- **Test de Idoneidad (Suitability)**: Evalúa el perfil de riesgo del cliente
- **Test de Conveniencia (Convenience)**: Determina qué familias de productos son adecuadas
- **Test de Sostenibilidad (Sustainability)**: Evalúa las preferencias de inversión sostenible

## 👥 Autor

Singular Bank Development Team

## ✨ Características

- **Spring Boot 3.5.8** con Java 21
- **PostgreSQL** como base de datos principal
- **Liquibase** para migración y versionado de base de datos
- **OpenAPI/Swagger** para documentación de API
- **MapStruct** para mapeo de DTOs
- **Caffeine Cache** para optimización de rendimiento
- **OpenTelemetry** para trazabilidad distribuida
- **Karate Framework** para tests E2E
- **JaCoCo** para cobertura de código
- **SpotBugs + FindSecBugs** para análisis estático de código y seguridad
- **Actuator** para métricas y health checks

## 📦 Requisitos Previos

- **Java 21** o superior
- **Maven 3.8** o superior
- **Docker** o **Podman** (para base de datos local)
- **PostgreSQL 15.13** (para entornos productivos)


## 🚀 Configuración

### 1. Base de Datos Local (Docker/Podman)

La base de datos PostgreSQL se levanta mediante Docker Compose:

```bash
cd docker
docker-compose up -d
# o usando Podman
podman-compose up -d
```

### 2. Configuración de Propiedades

Las propiedades se configuran en:
- `src/main/resources/application.yml` (configuración común)
- `src/main/resources/application-local.yml` (desarrollo local)

### 3. Migración de Base de Datos

Liquibase se ejecuta automáticamente al iniciar la aplicación y aplica los changesets en:
```
liquibase/
├── changelog-master.yaml
└── changes/
    ├── dev/              # Datos de desarrollo
    └── schema/           # Estructura de tablas
```

## 🏃 Ejecución

### Desarrollo Local

```bash
# 1. Levantar PostgreSQL
cd docker && docker-compose up -d

# 2. Ejecutar la aplicación
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

La aplicación estará disponible en: **http://localhost:8081**

### Compilar

```bash
mvn clean install
```

### Ejecutar Tests

```bash
# Tests unitarios
mvn test

# Tests de Karate (E2E)
mvn clean test -Dtest=KarateRunnerIT

# Tests con reporte de cobertura JaCoCo
mvn clean verify

# Análisis estático con SpotBugs
mvn spotbugs:check
```

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/singularbank/mifid/
│   │   ├── annotation/          # Anotaciones personalizadas (@ServiceTimeout, @EnsureIdentificationClient)
│   │   ├── config/              # Configuraciones (Cache, Database, Security, Tracing)
│   │   │   ├── controller/      # Controladores REST por dominio
│   │   │   ├── answer/          # SaveAnswersController
│   │   │   ├── convenience/     # Convenience tests y alertas
│   │   │   ├── customer/        # CurrentCustomerTestsController
│   │   │   ├── history/         # HistoryTestController       
│   │   │   ├── status/          # StatusTestController     
│   │   │   ├── suitability/     # Suitability tests
│   │   │   ├── sustainability/  # Sustainability tests
│   │   │   ├── download/        # PDF download
│   │   │   └── diagnostic/      # Health checks personalizados
│   │   ├── entity/              # Entidades del dominio (no JPA)
│   │   ├── repository/          # Capa de persistencia
│   │   │   └── database/        # Implementación con JPA/PostgreSQL
│   │   │       ├── jpa/         # Repositorios JPA
│   │   │       ├── mapper/      # Mappers Entity <-> Domain
│   │   │       └── model/       # Entidades JPA
│   │   ├── service/             # Lógica de negocio
│   │   │   ├── answer/          # Guardado de respuestas
│   │   │   ├── convenience/     # Tests de conveniencia
│   │   │   ├── customer/        # Información de cliente
│   │   │   ├── history/         # Historial de tests           
│   │   │   ├── status/          # Gestión de estados        
│   │   │   ├── pdf/             # Generación de PDFs
│   │   │   ├── suitability/     # Tests de idoneidad
│   │   │   └── sustainability/  # Tests de sostenibilidad
│   │   ├── exception/           # Manejo centralizado de excepciones
│   │   └── utils/               # Utilidades
│   └── resources/
│       ├── application.yml
│       └── application-local.yml
└── test/
    ├── java/
    │   └── karate/              # Tests E2E con Karate
    └── resources/
        └── karate/
            ├── config/          # karate-config.js
            └── features/        # 15 feature files organizados por dominio
```

## 📚 Documentación de la API

La documentación interactiva de la API está disponible en:

- **Swagger UI**: http://localhost:8081/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs
- **OpenAPI YAML**: http://localhost:8081/v3/api-docs.yaml

### Principales Endpoints

#### Tests de Idoneidad (Suitability)
- `GET /api/v1/test-mifid/suitability/{service}` - Obtener preguntas del test
- `GET /api/v1/test-mifid/suitability-responses/{testId}` - Obtener respuestas guardadas
- `POST /api/v1/test-mifid/suitability/{documentNumber}` - Guardar respuestas

#### Tests de Conveniencia (Convenience)
- `GET /api/v1/test-mifid/convenience/{service}` - Obtener preguntas del test
- `POST /api/v1/test-mifid/convenience/{documentNumber}` - Guardar respuestas
- `POST /api/v1/test-mifid/convenience/alerts/{documentNumber}` - Calcular alertas

#### Tests de Sostenibilidad (Sustainability)
- `GET /api/v1/test-mifid/sustainability/{service}` - Obtener preguntas del test
- `POST /api/v1/test-mifid/sustainability/{documentNumber}` - Guardar respuestas

#### Servicios Generales
- `POST /api/v1/test-mifid/answers/{documentNumber}` - Guardar respuestas de múltiples tests
- `GET /api/v1/test-mifid/current-customer-tests/{documentNumber}` - Obtener tests activos
- `GET /api/v1/test-mifid/pdf/{documentNumber}` - Descargar PDF del test

#### Historial de Tests
- `GET /api/v1/test-mifid/{document-number}/tests` - Obtener historial de tests del cliente

**Parámetros de query:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `type` | string | Filtrar por tipo: `CONVENIENCE`, `SUITABILITY`, `SUSTAINABILITY` |
| `state` | string | Filtrar por estado: `DRAFT`, `PENDING`, `SIGNED`, `CANCELLED`, `EXPIRED` |
| `from` | date | Fecha inicio (formato: `YYYY-MM-DD`) |
| `to` | date | Fecha fin (formato: `YYYY-MM-DD`) |
| `page` | int | Número de página (default: 0) |
| `size` | int | Tamaño de página (default: 20) |

#### Gestión de Estado
- `PUT /api/v1/test-mifid/responses/{test-id}/status` - Actualizar estado de un test

**Estados válidos:** `DRAFT`, `PENDING`, `SIGNED`, `CANCELLED`

**Transiciones permitidas:**
| Estado Actual | Estados Permitidos |
|---------------|-------------------|
| `DRAFT` | `DRAFT`, `PENDING`, `SIGNED`, `CANCELLED` |
| `PENDING` | `SIGNED`, `CANCELLED` |
| `SIGNED` | _(estado final)_ |
| `CANCELLED` | _(estado final)_ |
| `EXPIRED` | _(estado final - automático)_ |

**Nota:** `EXPIRED` no es un estado transicionable manualmente; se asigna automáticamente cuando caduca un test firmado.

#### Health Checks
- `GET /actuator/health` - Estado de salud de la aplicación
- `GET /actuator/info` - Información de la aplicación
- `GET /actuator/metrics` - Métricas de la aplicación
- `GET /health/liveness` - Liveness probe (Kubernetes)
- `GET /health/readiness` - Readiness probe (Kubernetes)

## 💾 Sistema de Caché

El servicio implementa **Caffeine Cache** para optimizar el rendimiento de las consultas frecuentes.

### Características

- **Proveedor**: Caffeine (alto rendimiento en memoria)
- Expiración automática basada en tiempo (TTL)
- Límite de entradas configurable
- Estadísticas y métricas disponibles vía Actuator
- Invalidación automática en operaciones de escritura

### Configuración por Entorno

| Entorno | TTL | Max Entradas |
|---------|-----|--------------|
| Local | 10s | 1000 |
| Test | 60s | 1000 |
| UAT | 600s | 1000 |
| Producción | 1800s (30 min) | 1000 |

### Cachés Configurados

- `questions` - Preguntas de los tests
- `versions` - Versiones de tests
- `customerTests` - Tests activos del cliente
- `testResults` - Resultados de tests

### Métricas de Caché

Disponibles en `/actuator/metrics`:

```bash
# Ver hits del caché
curl http://localhost:8081/actuator/metrics/cache.gets.hit

# Ver misses del caché
curl http://localhost:8081/actuator/metrics/cache.gets.miss

# Ver evictions
curl http://localhost:8081/actuator/metrics/cache.evictions.total
```

### Interpretación

- **Hit Rate > 80%**: Excelente rendimiento
- **Hit Rate 60-80%**: Buen rendimiento
- **Hit Rate < 60%**: Considerar ajustar TTL o tamaño máximo

## Manejo de Errores

Respuestas estandarizadas para errores comunes:

- **404**: Recurso no encontrado
- **400**: Petición inválida o error de validación
- **409**: Conflicto de datos
- **500**: Error interno del servidor

## Ejecución

### Requisitos
- Java 21
- Maven 3.8+
- PostgreSQL (solo producción)

### Comandos

Compilar:
```bash
mvn clean install
```

Ejecutar tests:
```bash
mvn test
```

Ejecutar en local:
```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

Ejecutar en producción:
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## Documentación API

La documentación interactiva está disponible en:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml


## Ejemplos de Uso

### Ver configuración
```bash
curl -X GET http://localhost:8080/api/config
```

## Configuración de Timeouts

El servicio implementa un sistema flexible de timeouts que permite configurar tiempos de espera tanto a nivel global como por endpoint específico.

### Timeout Global

El timeout global se configura en los archivos de propiedades (`application-{environment}.properties`) mediante la propiedad:

```properties
app.api.timeout=30000  # Valor en milisegundos
```

Este valor se utiliza como timeout por defecto para todos los endpoints que no especifiquen un timeout personalizado.

### Timeout por Servicio

Para configurar un timeout específico para un endpoint, se utiliza la anotación `@ServiceTimeout` en el método del controlador:

```java
@GetMapping("/{documentNumber}")
@ServiceTimeout(5000)  // Timeout de 5 segundos para esta operación
public ResponseEntity<CustomerResponseDTO> getCustomerByDocumentNumber(@PathVariable String documentNumber) {
    return ResponseEntity.ok(customerService.getCustomerByDocumentNumber(documentNumber));
}
```

#### Ejemplos de uso:

```java
// Timeout personalizado de 10 segundos
@PostMapping
@ServiceTimeout(10000)
public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody CustomerRequestDTO request) { ... }

// Usa el timeout global definido en properties
@GetMapping
public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() { ... }
```

### Comportamiento del Timeout

- Si se excede el tiempo configurado, la petición se cancelará y se retornará un error HTTP 408 (Request Timeout)
- El tiempo se mide desde que la petición llega al controlador hasta que se completa la respuesta
- Los timeouts son aplicados a nivel de controlador mediante AOP (Aspect-Oriented Programming)

### Valores de Timeout por Ambiente

Los valores por defecto configurados en cada ambiente son:

```properties
# _application-local.properties
app.api.timeout=5000  # 5 segundos

# application-dev.properties
app.api.timeout=30000  # 30 segundos

# application-uat.properties
app.api.timeout=25000  # 25 segundos

# application-prod.properties
app.api.timeout=30000  # 30 segundos
```

# Karate Tests - SVC-MIFID-PROCESS-JAVA

## 📋 Descripción

Tests de integración end-to-end usando [Karate Framework](https://github.com/karatelabs/karate) para validar los endpoints de la API MiFID.

## 🗂️ Estructura

```
src/test/
├── java/com/singularbank/mifid/karate/
│   └── KarateRunner.java                   # Runner principal de tests
└── resources/com/singularbank/mifid/karate/
    ├── config/
    │   └── karate-config.js                 # Configuración global
    └── features/
        ├── answers/                          # Tests de guardado de respuestas
        │   ├── Answers - Save Multiple Tests.feature
        │   └── Answers - Save Validations.feature
        ├── convenience/                      # Tests de conveniencia
        │   ├── Convenience - Get Test Questions.feature
        │   ├── Convenience - Get Test Validations.feature
        │   └── Convenience - Get Answers Validations.feature
        ├── convenience-alerts/               # Tests de cálculo de alertas
        │   ├── Convenience - Calculate Alerts.feature
        │   └── Convenience - Calculate Alerts Validations.feature
        ├── customer/                         # Tests de información de cliente
        │   ├── Customer - Get Active Tests.feature
        │   └── Customer - Get Active Tests Validations.feature
        ├── suitability/                      # Tests de idoneidad
        │   ├── Suitability - Get Test Questions.feature
        │   ├── Suitability - Get Test Validations.feature
        │   └── Suitability - Get Answers Validations.feature
        └── sustainability/                   # Tests de sostenibilidad
            ├── Sustainability - Get Test Questions.feature
            ├── Sustainability - Get Test Validations.feature
            └── Sustainability - Get Answers Validations.feature
```

## 🚀 Ejecución

### Ejecutar TODOS los tests de Karate

```bash
mvn clean test -Dtest=KarateRunner
```

📊 **Reporte**: `target/karate-reports/karate-summary.html`

### Ejecutar tests por categoría (usando tags)

#### Tests de Suitability (Idoneidad)
```bash
mvn clean test -Dtest=KarateRunner -Dkarate.options="--tags @suitability"
```

#### Tests de Convenience (Conveniencia)
```bash
mvn clean test -Dtest=KarateRunner -Dkarate.options="--tags @convenience"
```

#### Tests de Sustainability (Sostenibilidad)
```bash
mvn clean test -Dtest=KarateRunner -Dkarate.options="--tags @sustainability"
```

#### Tests de Servicios Generales (Answers, Customer, Alerts)
```bash
mvn clean test -Dtest=KarateRunner -Dkarate.options="--tags @answers,@customer,@convenience-alerts"
```

### Ejecutar por tags específicos

```bash
# Solo smoke tests
mvn clean test -Dtest=KarateRunner -Dkarate.options="--tags @smoke"

# Solo validaciones
mvn clean test -Dtest=KarateRunner -Dkarate.options="--tags @validations"

# Solo happy path
mvn clean test -Dtest=KarateRunner -Dkarate.options="--tags @happy-path"
```

## ⚙️ Configuración

### Entornos

El archivo `karate-config.js` soporta múltiples entornos:

```bash
# Desarrollo local (default)
mvn clean test -Dtest=KarateRunner

# UAT
mvn clean test -Dtest=KarateRunner -Dkarate.env=uat
```

### Variables de configuración

En `karate-config.js`:

```javascript
{
  env: 'dev',
  baseUrl: 'http://localhost:8081/api/v1',
  timeout: 10000,
  testData: {
    documentNumber: '12345678A',
    version: 1
  }
}
```

## 🏷️ Tags disponibles

- `@smoke` - Tests principales de cada feature
- `@happy-path` - Casos de éxito
- `@validations` - Tests de validaciones y errores
- `@suitability` - Tests de idoneidad
- `@convenience` - Tests de conveniencia
- `@convenience-alerts` - Tests de cálculo de alertas
- `@sustainability` - Tests de sostenibilidad
- `@answers` - Tests de guardado de respuestas
- `@customer` - Tests de información de cliente
- `@test-history` - Tests de historial de tests      
- `@test-status` - Tests de gestión de estado        

## 📊 Reportes

Después de ejecutar los tests, el reporte HTML se genera automáticamente en:

```
target/karate-reports/karate-summary.html
```

El reporte muestra:
- ✅ Todos los features organizados alfabéticamente por nombre descriptivo
- ✅ Agrupación visual por categoría (Answers, Convenience, Customer, etc.)
- ✅ Estado de cada escenario (passed/failed)
- ✅ Tiempo de ejecución

Para ver el reporte:
```bash
open target/karate-reports/karate-summary.html
```

## 📝 Cobertura de Tests

### SaveAnswersController
- ✅ Guardar respuestas de múltiples tests (Suitability, Convenience, Sustainability)
- ✅ Validaciones de campos obligatorios
- ✅ Validación de formato de documento

### CurrentCustomerTestsController
- ✅ Obtener tests activos del cliente
- ✅ Validación de formato de documento
- ✅ Cliente no existente

### ConvenienceTestAlertsController
- ✅ Calcular alertas de conveniencia
- ✅ Validaciones de campos obligatorios
- ✅ Validación de formato de documento

### Suitability/Convenience/Sustainability Tests
- ✅ Obtener preguntas del test
- ✅ Obtener respuestas guardadas
- ✅ Guardar respuestas
- ✅ Validaciones completas (formato documento, servicio no válido, versión incorrecta)

## 📝 Escribir nuevos tests

### Ejemplo básico:

```gherkin
@my-feature @happy-path
Feature: Mi Nuevo Feature - Descripción Clara

  Background:
    * url baseUrl
    * def correlationId = 'test-' + java.util.UUID.randomUUID()

  @smoke
  Scenario: Descripción del caso de prueba
    Given path 'test-mifid', 'my-endpoint'
    And header X-Correlation-ID = correlationId
    When method GET
    Then status 200
    And match response.data == '#notnull'
```

### Convenciones de nombres de archivos:

- Usar formato: `{Categoría} - {Descripción}.feature`
- Ejemplos:
  - `Suitability - Get Test Questions.feature`
  - `Answers - Save Multiple Tests.feature`
  - `Customer - Get Active Tests.feature`

Esto mejora la legibilidad en el reporte HTML.

## 🔍 Validaciones comunes

### Validar estructura de respuesta:
```gherkin
And match response == 
"""
{
  testId: '#number',
  version: '#number',
  questions: '#array'
}
"""
```

### Validar array:
```gherkin
And match response.questions == '#array'
And match response.questions[0].id == '#number'
```

### Validar campos opcionales:
```gherkin
And match response.optionalField == '#notpresent'  # Campo no existe
And match response.nullableField == '#null'        # Campo existe pero es null
```

### Validar status codes:
```gherkin
Then status 200  # OK
Then status 201  # Created
Then status 400  # Bad Request
Then status 404  # Not Found
```

## 📚 Recursos

- [Documentación oficial de Karate](https://github.com/karatelabs/karate)
- [Karate syntax reference](https://github.com/karatelabs/karate#syntax-guide)
- [Ejemplos de Karate](https://github.com/karatelabs/karate/tree/master/karate-demo)

## ⚠️ Notas importantes

1. **Usar `mvn clean test`** - Siempre limpiar antes de ejecutar para evitar archivos duplicados
2. **Los tests requieren el servidor corriendo** - Son tests E2E, no unitarios
3. **Usar datos de test** - No usar datos de producción
4. **Correlation ID** - Siempre incluir para trazabilidad
5. **Nombres descriptivos** - Los nombres de archivos feature se muestran en el reporte HTML

## 🆚 Diferencia con tests MockMvc

| Aspecto | Karate | MockMvc |
|---------|--------|---------|
| **Tipo** | E2E / API | Unitario / Integración |
| **Servidor** | Real (localhost) | Mock (Spring Test) |
| **Lenguaje** | Gherkin | Java |
| **Velocidad** | Más lento | Más rápido |
| **Uso** | Validación E2E | Tests unitarios |

**Recomendación**: Usar ambos complementariamente:
- **MockMvc**: Para tests rápidos de lógica y validaciones
- **Karate**: Para tests E2E y validación de contratos API

### Consideraciones

- Los timeouts específicos por servicio tienen prioridad sobre el timeout global
- Si un servicio no tiene la anotación `@ServiceTimeout`, se utilizará el timeout global
- Los timeouts se configuran en milisegundos
- Se recomienda ajustar los timeouts según la complejidad y necesidades específicas de cada operación 
---

## 📊 Resumen Técnico

| Componente | Tecnología | Versión |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.5.8 |
| **Java** | OpenJDK | 21 |
| **Base de Datos** | PostgreSQL | 15.13 |
| **Migración DB** | Liquibase | - |
| **Caché** | Caffeine | - |
| **Mapeo** | MapStruct | 1.5.5 |
| **Tests E2E** | Karate | 1.4.1 |
| **Cobertura** | JaCoCo | 0.8.14 |
| **Análisis Estático** | SpotBugs | 4.9.8.2 |
| **Seguridad** | FindSecBugs | 1.14.0 |
| **Trazabilidad** | OpenTelemetry | 2.8.0 |
| **Documentación** | SpringDoc OpenAPI | 2.8.9 |

## 🧪 Calidad de Código y Cobertura

### JaCoCo - Cobertura de Código

El proyecto utiliza **JaCoCo (Java Code Coverage)** para medir la cobertura de tests.

#### Ejecutar análisis de cobertura

```bash
# Generar reporte de cobertura
mvn clean verify

# Solo tests unitarios
mvn clean test

# Tests de integración
mvn clean integration-test
```

#### Reportes generados

Después de ejecutar `mvn clean verify`, el reporte consolidado se encuentra en:

- **Reporte único**: `target/site/jacoco-merged/index.html`

```bash
# Abrir reporte consolidado
open target/site/jacoco-merged/index.html
```

#### Configuración por fase

| Fase | Goal | Descripción |
|------|------|-------------|
| `test` | `prepare-agent` | Prepara agente para tests unitarios |
| `test` | `report` | Genera reporte de tests unitarios |
| `integration-test` | `prepare-agent-integration` | Prepara agente para tests de integración |
| `post-integration-test` | `report-integration` | Genera reporte de integración |
| `verify` | `merge` | Fusiona resultados unitarios + integración |
| `verify` | `merged-report` | Genera reporte consolidado |

#### Métricas de cobertura

El reporte HTML muestra:
- **Líneas** cubiertas vs. totales
- **Ramas** (branches) cubiertas
- **Complejidad ciclomática**
- **Métodos** y **clases** cubiertos

### SpotBugs - Análisis Estático

**SpotBugs** analiza el bytecode Java para detectar bugs potenciales y malas prácticas.

#### Ejecutar análisis

```bash
# Ejecutar SpotBugs
mvn spotbugs:spotbugs

# Ejecutar con verificación (falla si encuentra bugs críticos)
mvn spotbugs:check

# Ver reporte en la consola
mvn spotbugs:gui
```

#### Reportes

```bash
# Ver reporte HTML
open target/spotbugs.html

# Reporte XML para CI/CD
cat target/spotbugsXml.xml
```

#### Configuración

```xml
<configuration>
  <effort>Max</effort>           <!-- Nivel de análisis: Min, Default, Max -->
  <threshold>Medium</threshold>  <!-- Severidad: Low, Medium, High -->
  <failOnError>false</failOnError> <!-- No falla el build por defecto -->
</configuration>
```

### FindSecBugs - Análisis de Seguridad

**FindSecBugs** es un plugin de SpotBugs especializado en detectar vulnerabilidades de seguridad.

#### Tipos de vulnerabilidades detectadas

- **Inyección SQL**
- **XSS (Cross-Site Scripting)**
- **Deserialización insegura**
- **Criptografía débil**
- **Path Traversal**
- **Command Injection**
- **Uso inseguro de Random**
- **Exposición de información sensible**

#### Integración con SpotBugs

FindSecBugs se ejecuta automáticamente con SpotBugs:

```bash
mvn spotbugs:check
```

El análisis incluye tanto bugs generales (SpotBugs) como vulnerabilidades de seguridad (FindSecBugs).

### Pipeline de Calidad Completo

```bash
# 1. Compilar
mvn clean compile

# 2. Tests unitarios + cobertura
mvn test

# 3. Tests de integración (Karate)
mvn integration-test

# 4. Análisis estático
mvn spotbugs:check

# 5. Generar todos los reportes
mvn verify

# Todo en un solo comando
mvn clean verify spotbugs:check
```

### Recomendaciones de Calidad

#### Cobertura de código

**Estado actual**: ✅ **75.95%** (superando el objetivo del 70%)

```bash
# Ejecutar TODOS los tests (unitarios + integración) y generar reporte
mvn clean verify

# Ver reporte HTML
open target/site/jacoco/index.html
```

**El reporte incluye**:
- 313 tests unitarios
- 141 tests de integración (con TestContainers)
- **Total**: 454 tests con **75.95% de cobertura**

**Validación automática**:
- El build falla si la cobertura baja del **70%**
- Se valida automáticamente en fase `verify`

**Objetivos**:
- ✅ Mínimo: 70% (ALCANZADO)
- 🎯 Recomendado: 80%

#### Análisis estático
- Ejecutar SpotBugs antes de cada commit
- Revisar y corregir bugs de prioridad **High** y **Medium**
- No ignorar warnings de seguridad de FindSecBugs

#### Integración continua
```yaml
# Ejemplo para pipeline CI/CD
- mvn clean verify spotbugs:check
- if [ $? -eq 0 ]; then echo "✅ Calidad OK"; fi
```

### Visualización en IDE

#### IntelliJ IDEA
- Plugin: **SpotBugs-IDEA**
- Settings → Plugins → Marketplace → "SpotBugs"
- Analiza mientras codificas

#### Eclipse
- Plugin: **SpotBugs Eclipse Plugin**
- Help → Eclipse Marketplace → "SpotBugs"

#### VS Code
- Extension: **SpotBugs**
- Requiere Maven/Gradle configurado

## 🔒 Seguridad

### Estado Actual

La seguridad JWT está **DESHABILITADA** por defecto:

```yaml
# application.yml
security:
  enabled: false
```

### Arquitectura de Seguridad

Implementación basada en **svc-onboarding** con:
- **JWT Authentication**: Validación de tokens JWT
- **OAuth2 Resource Server**: Spring Security OAuth2
- **Sin roles por ahora**: Solo autenticación básica (preparado para roles futuros)
- **Condicional**: Se activa/desactiva con `security.enabled`

### Endpoints

| Endpoint | Estado | Descripción |
|----------|--------|-------------|
| `/health/**` | 🌍 Público | Health checks |
| `/diagnostic/**` | 🌍 Público | Diagnósticos |
| `/actuator/**` | 🌍 Público | Métricas |
| `/swagger-ui/**` | 🌍 Público | Documentación |
| `/v3/api-docs/**` | 🌍 Público | OpenAPI |
| `/api/v1/test-mifid/**` | 🔒 Protegido* | API MiFID |

*Solo cuando `security.enabled=true`

### Habilitar Seguridad

**Opción 1: Variable de entorno**
```bash
export SECURITY_ENABLED=true
```

**Opción 2: application.yml**
```yaml
security:
  enabled: true
```

**Opción 3: Profile específico**
```yaml
# application-prod.yml
security:
  enabled: true
```

### Uso con JWT

Cuando la seguridad esté habilitada:

```bash
# Petición autenticada
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8081/api/v1/test-mifid/suitability/ONBOARDING?version=1
```

**Formato JWT esperado:**
```json
{
  "sub": "user-id",
  "email": "usuario@singularbank.com",
  "iat": 1701619200,
  "exp": 1701622800
}
```

### Testing con Seguridad

**Tests unitarios**: Sin cambios (Spring Security se mockea automáticamente)

**Tests Karate con JWT**:
```gherkin
Background:
  * def jwtToken = 'eyJhbGc...'
  * header Authorization = 'Bearer ' + jwtToken
```

### Componentes de Seguridad

- **SecurityConfig**: Define filtros condicionales según `security.enabled`
- **SimpleJwtDecoder**: Decodifica y valida tokens JWT
- **JwtAuthenticationConverter**: Extrae usuario del JWT (email o sub)

### Próximos Pasos

Cuando se definan requisitos de autorización:
1. Definir roles (ADVISOR, ADMIN, etc.)
2. Actualizar `JwtAuthenticationConverter` para extraer roles
3. Añadir `@PreAuthorize` en controllers
4. Configurar validación de firma JWT con clave pública

## 🚀 Quick Start

```bash
# 1. Levantar base de datos
cd docker && docker-compose up -d

# 2. Ejecutar aplicación
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# 3. Acceder a Swagger
open http://localhost:8081/swagger-ui/index.html

# 4. Ejecutar tests
mvn clean test -Dtest=KarateRunner
```

## 📞 Soporte

Para dudas o problemas técnicos, contacta al equipo de desarrollo de Singular Bank.

