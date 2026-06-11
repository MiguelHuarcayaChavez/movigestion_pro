# Spec: Módulo de Incidencias (005-incident.spec.md)

## Propósito
Gestionar la emisión y consulta de reportes de eventualidades en ruta (accidentes, fallas mecánicas, tráfico). Este módulo garantiza que la telemetría enviada por los conductores sea inmutable y autocompleta el contexto logístico de forma transparente para evitar errores de digitación en momentos críticos.

---

## 1. Contrato (API Protocol)

Todas las rutas requieren la cabecera `Authorization: Bearer <token>`.

### Endpoint 1: Registrar Incidencia (Create)
* **Verbo y URL:** `POST /api/v1/incidents`
* **Seguridad:** Privado (Exclusivo para `ROLE_DRIVER`).
* **Request Body (CreateIncidentRequestDTO):**
  *(Nota: El conductor NO envía su ID, ni el ID del viaje, ni el ID del vehículo. Solo envía lo que está pasando en ese instante).*
    ```json
    {
      "clasificacion": "VEHICULO",
      "descripcion": "Llanta delantera derecha reventada en el km 45 de la Panamericana Sur.",
      "latitudReporte": -12.345678,
      "longitudReporte": -76.543210
    }
    ```
* **Respuestas Esperadas:**
    * `201 Created`: Retorna el reporte guardado con los IDs inyectados por el backend.
        ```json
        {
          "id_incidencia": 89,
          "id_envio": 45,
          "id_vehiculo": 2,
          "id_transportista": 16,
          "clasificacion": "VEHICULO",
          "fecha_reporte": "2026-05-30T15:30:00Z"
        }
        ```
    * `400 Bad Request`: Validaciones (ej. coordenadas inválidas o clasificación que no está en el ENUM).
    * `422 Unprocessable Entity`: El chofer no tiene un viaje activo en curso.

### Endpoint 2: Monitoreo Global de Incidencias (Read - Admin)
* **Verbo y URL:** `GET /api/v1/incidents`
* **Seguridad:** Privado (`ROLE_ADMIN`).
* **Respuestas Esperadas:**
    * `200 OK`: Arreglo `[]` con el histórico global de incidencias. (Diseñado para integrarse con el Short Polling del dashboard). Bloqueado para `ROLE_DRIVER`.

### Endpoint 3: Historial Personal de Incidencias (Read - Driver)
* **Verbo y URL:** `GET /api/v1/incidents/my-incidents`
* **Seguridad:** Privado (`ROLE_DRIVER`).
* **Respuestas Esperadas:**
    * `200 OK`: Arreglo `[]` con las incidencias reportadas únicamente por el chofer autenticado.

---

## 2. Dominio (Mapeo de Entidades y Reglas de Negocio)

### Entidades y Persistencia (`database.md`)
* Interacción principal con la entidad `Incidencia` (tabla `incidencia`).
* Para ejecutar el `POST`, el servicio requerirá consultas de lectura sobre `Envio` para resolver el contexto logístico.

### Aplicación de Business Rules (`business_rule.md`)
* **Implementar BR-INC-01 (Inyección Automática de Contexto):** Cuando el controlador recibe el `POST`, el servicio debe:
    1. Extraer el `id_usuario` del JWT (`SecurityContextHolder`).
    2. Buscar en la tabla `Envio` un registro donde `id_transportista == id_usuario` Y `estado == 'EN_CAMINO'`.
    3. Si existe, extraer el `id_envio` y el `id_vehiculo` de ese registro y guardarlos en la nueva `Incidencia`. Si no existe, rechazar la operación.
* **Implementar BR-INC-02 (Inmutabilidad de Reportes):** Ausencia por diseño. Queda estrictamente prohibida la creación de controladores o servicios que respondan a los verbos `PUT`, `PATCH` o `DELETE` para la ruta `/api/v1/incidents/**`.

---

## 3. Validación (Criterios de Aceptación para Pruebas)

Al generar el código, el asistente debe prever los siguientes flujos para las pruebas unitarias:

### Flujos de `POST /api/v1/incidents`
* ✅ **Happy Path:** Un conductor con un viaje `EN_CAMINO` envía sus coordenadas y el motivo de su avería. El backend intercepta su identidad, busca su viaje activo, vincula el camión que está manejando en ese instante, guarda el registro con un `CURRENT_TIMESTAMP` y retorna `201 Created`.
* ❌ **Unhappy Path 1 (Bloqueo de Falsos Reportes / Fuera de Ruta):** Un conductor que está en estado `DISPONIBLE` (o cuyo viaje está `ASIGNADO` pero aún no ha iniciado) intenta enviar una incidencia. El servicio no logra encontrar un viaje `EN_CAMINO` vinculado a su ID y arroja una excepción de negocio retornando `422 Unprocessable Entity` con el mensaje: *"No se puede emitir la incidencia: No cuenta con un viaje activo en ruta en este momento."*
* ❌ **Unhappy Path 2 (Validación de DTO Strict):** El cliente de Angular envía una clasificación tipiada incorrectamente (ej. `"clasificacion": "CLIMA"` en lugar de `"OTRO"`). La validación `@Valid` del enumerador bloquea la petición inmediatamente retornando `400 Bad Request` e indicando los valores permitidos en el arreglo `subErrors`.

### Flujos de Privacidad (`GET` endpoints)
* ✅ **Happy Path (Scope Enforcement):** Cuando un chofer invoca `/my-incidents`, el query de base de datos se construye utilizando inyectando el ID extraído de su token JWT, asegurando que es matemáticamente imposible que reciba por error la incidencia de otro compañero de flota.