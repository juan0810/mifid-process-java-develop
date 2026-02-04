# Configuración de Grafana y Prometheus

Este directorio contiene la configuración para monitoreo del servicio con Grafana.

## Archivos incluidos

- `GrafanaDashboard-Microservice-svc-onboarding-process-java.json` - Dashboard de Grafana preconfigurado para el microservicio

## Configuración de Prometheus

Para que Prometheus pueda recopilar las métricas del servicio, es necesario añadir la siguiente configuración en el archivo `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'svc-onboarding-process-java'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']    
        labels:
          app: 'svc-onboarding-process-java'
```

### Explicación de la configuración:

- **`job_name`**: Nombre del trabajo de scraping, utiliza el nombre del proyecto
- **`metrics_path`**: Ruta donde el servicio expone las métricas de Prometheus (`/actuator/prometheus`)
- **`targets`**: Dirección donde está ejecutándose el servicio
  - `host.docker.internal:8080` - Para acceder desde un contenedor Docker al host
  - Cambiar por `localhost:8080` si Prometheus no está en contenedor
- **`labels`**: Etiquetas adicionales para identificar el servicio en Prometheus

### Configuración según el entorno:

#### Desarrollo Local
```yaml
- targets: ['localhost:8080']
```

#### Docker/Contenedores
```yaml
- targets: ['host.docker.internal:8080']
```

#### Producción
```yaml
- targets: ['your-service-host:8080']
```

## Importar Dashboard en Grafana

1. Acceder a Grafana (normalmente en `http://localhost:3000`)
2. Ir a **Dashboards** → **Import**
3. Subir el archivo `GrafanaDashboard-Microservice-svc-onboarding-process-java.json`
4. Configurar la fuente de datos de Prometheus si es necesario

## Métricas disponibles

El servicio expone las siguientes métricas a través de Spring Boot Actuator:

- **JVM**: Memoria, threads, garbage collection
- **HTTP**: Requests, response times, status codes
- **Cache**: Hit/miss ratio, evictions (si Redis está habilitado)
- **Database**: Connection pool, query times (si JPA está habilitado)
- **Custom**: Métricas específicas del negocio

## Verificación

Para verificar que las métricas están disponibles:

```bash
# Verificar endpoint de métricas
curl http://localhost:8080/actuator/prometheus

# Verificar health check
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Problema: Prometheus no puede acceder al servicio

**Solución**: Verificar la configuración de `targets` según tu entorno:
- Local: `localhost:8080`
- Docker: `host.docker.internal:8080`
- Kubernetes: `service-name.namespace.svc.cluster.local:8080`

### Problema: No aparecen métricas en Grafana

**Solución**: 
1. Verificar que Prometheus esté scrapeando correctamente: `http://prometheus:9090/targets`
2. Verificar que la fuente de datos en Grafana esté configurada correctamente
3. Comprobar que el servicio esté exponiendo métricas en `/actuator/prometheus` 