# Spec: Módulo de Analíticas y Dashboard (006-analytics.spec.md)

## Propósito
Proveer métricas consolidadas en tiempo real e indicadores clave de rendimiento (KPIs) para el panel de control central del Administrador. Este módulo es de acceso estrictamente de lectura y se encarga de realizar agregaciones eficientes en la base de datos sin afectar la operación transaccional.

---

## 1. Contrato (API Protocol)

Todas las rutas requieren la cabecera `Authorization: Bearer <token>`.

### Endpoint 1: Obtener Métricas Generales del Dashboard
* **Verbo y URL:** `GET /api/v1/analytics/dashboard`
* **Seguridad:** Privado (Exclusivo para `ROLE_ADMIN`).
* **Query Params:** (Ninguno por defecto. Se asume que calcula métricas globales o del mes en curso según lógica de negocio).
* **Respuestas Esperadas:**
    * `200 OK`: Retorna el objeto unificado con todos los contadores de la operación logística.
        ```json
        {
          "totalViajesMes": 342,
          "porcentajeEntregasATiempo": 94.5,
          "vehiculosEnMantenimiento": 3,
          "vehiculosDisponibles": 12,
          "incidenciasCriticasActivas": 2,
          "conductoresEnRuta": 8
        }
        ```
    * `403 Forbidden`: Si un transportista intenta acceder a estas métricas globales.

---

## 2. Dominio (Mapeo de Entidades y Reglas de Negocio)

### Entidades y Persistencia (`database.md`)
* Este módulo no realiza mutaciones (ni `save`, ni `update`, ni `delete`).
* Interactúa en modo de solo lectura múltiple, inyectando los repositorios de `Envio`, `Vehiculo` e `Incidencia`.
* **Regla de Rendimiento (System Design):** Para evitar problemas de memoria en Spring Boot (excepción `OutOfMemoryError`), el servicio tiene **prohibido** traer listas enteras de objetos a Java mediante `findAll()` para contarlos. Las métricas deben resolverse mediante sentencias SQL nativas o JPQL optimizadas (`@Query("SELECT COUNT(v) FROM Vehiculo v WHERE v.estado = 'EN_MANTENIMIENTO'")`).

### Aplicación de Business Rules (`business_rule.md`)
* **Implementar BR-AUTH-03 (Exclusividad de Funciones):** El servicio expuesto es sensible comercialmente. La anotación de seguridad (ej. `@PreAuthorize("hasRole('ADMIN')")`) debe bloquear de raíz cualquier intento de consumo por parte de la flota operativa.
* **Integración Front-Back:** Este endpoint está diseñado para soportar el mecanismo de *Short Polling* definido en el punto 7 del diseño de sistema. Debe ser lo suficientemente ligero para responder a llamadas concurrentes cada 30 segundos desde el cliente de Angular.

---

## 3. Validación (Criterios de Aceptación para Pruebas)

Al generar el código, el asistente debe prever los siguientes flujos para las pruebas unitarias e integración:

### Flujos de `GET /api/v1/analytics/dashboard`
* ✅ **Happy Path:** Un Administrador autenticado ingresa al panel de control. El backend ejecuta las agregaciones SQL de forma eficiente y devuelve un estado `200 OK` con todos los KPIs debidamente calculados en formato numérico o decimal.
* ✅ **Happy Path 2 (Base de Datos Vacía):** Si el sistema acaba de ser instalado y no hay vehículos, viajes ni conductores en las tablas, el endpoint no debe arrojar errores aritméticos (como división por cero al calcular el porcentaje de entregas a tiempo). Debe manejar el caso devolviendo ceros (`0` o `0.0`) y un estado `200 OK`.
* ❌ **Unhappy Path 1 (Violación de Privilegios):** Un Conductor autenticado descubre la URL de la API mediante inspección de red en Angular e intenta hacer una petición GET. El filtro de Spring Security intercepta el rol `DRIVER`, detiene la ejecución inmediatamente y retorna `403 Forbidden`.