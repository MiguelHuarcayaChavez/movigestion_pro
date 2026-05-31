# Spec: Módulo de Vehículos (002-vehicle.spec.md)

## Propósito
Implementar el CRUD (Creación, Lectura, Actualización y Borrado lógico) de las unidades de transporte (flota). Este módulo asegura la integridad referencial de los vehículos y gestiona su ciclo de vida y operatividad documental (SOAT y Revisión Técnica).

---

## 1. Contrato (API Protocol)

Todas las rutas de este módulo son privadas y requieren la cabecera `Authorization: Bearer <token>` con el rol `ROLE_ADMIN`.

### Endpoint 1: Registrar Vehículo (Create)
* **Verbo y URL:** `POST /api/v1/vehicles`
* **Request Body (CreateVehicleRequestDTO):**
    ```json
    {
      "placa": "ABC-123",
      "fechaVencimientoSoat": "2027-12-31",
      "fechaVencimientoRevisionTecnica": "2026-11-15"
    }
    ```
* **Respuestas Esperadas:**
    * `201 Created`:
        ```json
        {
          "id_vehiculo": 2,
          "placa": "ABC-123",
          "estado": "DISPONIBLE",
          "fecha_vencimiento_soat": "2027-12-31",
          "fecha_vencimiento_revision": "2026-11-15"
        }
        ```
    * `400 Bad Request`: Validaciones de formato fallidas (ej. placa vacía o fechas inválidas).
    * `409 Conflict`: La placa ya existe en el sistema.

### Endpoint 2: Listar Vehículos (Read)
* **Verbo y URL:** `GET /api/v1/vehicles`
* **Query Params (Opcionales):** `?estado=DISPONIBLE` (Permite filtrar el catálogo).
* **Respuestas Esperadas:**
    * `200 OK`: Retorna un arreglo `[]` con los vehículos que coinciden con el filtro.

### Endpoint 3: Actualizar Vehículo (Update)
* **Verbo y URL:** `PUT /api/v1/vehicles/{id}`
* **Request Body (UpdateVehicleRequestDTO):**
    ```json
    {
      "estado": "EN_MANTENIMIENTO",
      "fechaVencimientoSoat": "2028-01-15",
      "fechaVencimientoRevisionTecnica": "2027-05-20"
    }
    ```
* **Respuestas Esperadas:**
    * `200 OK`: Retorna el vehículo con los datos actualizados.
    * `404 Not Found`: El ID proporcionado no existe en la base de datos.
    * `422 Unprocessable Entity`: Intento de cambiar el estado a un vehículo que actualmente está `EN_RUTA` (Regla de integridad).

### Endpoint 4: Dar de Baja Vehículo (Delete Lógico)
* **Verbo y URL:** `DELETE /api/v1/vehicles/{id}`
* **Respuestas Esperadas:**
    * `200 OK`: El estado del vehículo cambió a `DE_BAJA`.
    * `404 Not Found`: El ID proporcionado no existe.
    * `422 Unprocessable Entity`: No se puede dar de baja si el vehículo está `EN_RUTA`.

---

## 2. Dominio (Mapeo de Entidades y Reglas de Negocio)

### Entidades y Persistencia (`database.md`)
* El servicio interactuará con la entidad `Vehiculo`, mapeada a la tabla `vehiculo`.
* Al crear (`POST`), el campo `estado` debe inicializarse obligatoriamente por defecto en `DISPONIBLE` (`VehicleStatusEnum.DISPONIBLE`). El campo `fecha_creacion` es gestionado automáticamente por la base de datos o JPA (`@CreationTimestamp`).

### Aplicación de Business Rules (`business_rule.md`)
* **Implementar BR-DATA-01:** El método de creación debe capturar violaciones de restricción única (`DataIntegrityViolationException`) en la columna `placa` y traducirlo a un error HTTP `409 Conflict`.
* **Implementar BR-DATA-02:** El endpoint `DELETE` tiene prohibido invocar `repository.deleteById()`. En su lugar, debe buscar la entidad y ejecutar `vehiculo.setEstado(VehicleStatusEnum.DE_BAJA)`, seguido de un `repository.save()`.
* **Protección de Transición (Regla Implícita):** En los endpoints `PUT` y `DELETE`, el servicio debe verificar el estado actual del vehículo. Si el vehículo está en estado `EN_RUTA`, se debe bloquear cualquier modificación estructural o de baja lógica, lanzando una excepción de negocio (`422 Unprocessable Entity`).

---

## 3. Validación (Criterios de Aceptación para Pruebas)

Al generar el código, el asistente debe prever los siguientes flujos para las pruebas unitarias e integración (JUnit/Mockito):

### Flujos de `POST /api/v1/vehicles`
* ✅ **Happy Path:** Se envía un JSON válido y con placa inédita. El servicio guarda la unidad en BD asignando automáticamente el estado `DISPONIBLE` y responde con `201 Created`.
* ❌ **Unhappy Path 1 (Conflicto de Placa):** Se intenta registrar una placa que ya existe (ej. "ABC-123"). El servicio captura el error de BD y responde `409 Conflict` con el mensaje: *"La placa ingresada ya se encuentra registrada en el sistema."*
* ❌ **Unhappy Path 2 (Validación de DTO):** Se envía el body con fechas en el pasado para el SOAT o una placa que no cumple el patrón alfanumérico. Se retorna `400 Bad Request` listando los errores específicos en `subErrors`.

### Flujos de `DELETE /api/v1/vehicles/{id}`
* ✅ **Happy Path:** Se recibe un ID válido de un vehículo en estado `DISPONIBLE` o `EN_MANTENIMIENTO`. El servicio actualiza su estado a `DE_BAJA` y responde `200 OK`.
* ❌ **Unhappy Path 1 (Bloqueo Operativo):** Se intenta dar de baja un vehículo cuyo estado actual en la base de datos es `EN_RUTA`. El servicio detiene la transacción para proteger la integridad del envío activo y retorna `422 Unprocessable Entity` con el mensaje: *"No se puede dar de baja un vehículo que actualmente se encuentra en ruta."*
* ❌ **Unhappy Path 2 (Entidad No Encontrada):** Se envía un ID que no existe en la base de datos. Se lanza `ResourceNotFoundException` devolviendo `404 Not Found`.